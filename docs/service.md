# Services

Services are the business logic workhorses of a Spring Boot application.  These classes
are annotated with `@Service`.  Usually they take constructor-injected resources such
as [repositories](repository.md) for persistent data access and/or templates for accessing other
functionality such as connecting to other applications or caching.

A Service method is the place to enforce a transactional boundary or utilize caching.

## EmployeeService

The `EmployeeService` executes the business logic on behalf of the
[Employee Controller](controller.md).

The Service exposes only DTO objects to the Controller, not any Entity objects.  This
decoupling exists so that implementation details from a Repository don't get exposed
in the interface to a client.  This Service translates Entity objects to DTOs manually,
but `MapStruct` can do this without boilerplate code or reflection.

### Transactions

Place a transaction surrounding a method by annotating the method with `@Transactional`.
Spring's Transaction Manager will commit the transaction if the method completes normally,
but it will roll back the transaction if the method throws an unchecked exception.
Notably, it will _not_ roll back the transaction if a checked exception is thrown.  Also,
exceptions that are caught within the method body aren't seen by the Transaction Manager,
so those caught exceptions do not affect the transaction.

If a method doesn't change any data, one can use `@Transactional(readOnly = true)`.  This
way, Hibernate doesn't need to create snapshots of any managed data for dirty-checking.

### Caching

The `EmployeeService` Service class utilizes caching, starting with the starter
dependency for general caching, `spring-boot-starter-cache`.  In particular, it
connects to [Redis](redis.md) as its caching implementation with the starter dependency
`spring-boot-starter-data-redis`.

Methods can take advantage of general caching with caching-related annotations.
- `@Cacheable(value, key)` - Wrap the annotated method with caching capability.
If the key exists in the cache named by "value" (a cache "hit"), then the
cache/key contents will be returned, and the method will NOT be called.  If the key does
not exist in the cache (a cache "miss"), then the method will be called, and the result
will be cached under the cache with the key.  This is known as the _cache-aside_ pattern.
- `@CacheEvict(value, key)` - When changing data, the cache must be kept up to
date.  This annotation invalidates the cache entry, named by "value", corresponding to
the key.  Refer to parameter names by prefixing the key with `#`, e.g. `#id`.
- `@CacheEvict(value, allEntries=true)` - When adding to data, the cache must be kept up to
date.  This annotation invalidates all cache entries in the cache named by "value".
- `@Caching(evict = {<@CacheEvict instances>})` - Invalidate all appropriate caches
by grouping caches to evict.  This application must invalidate caches for individual
employees, plus the cache for all employees, when employee data changes.

The `@Cacheable` and `@CacheEvict` annotation functionality only work because Spring
proxies the service class to call caching logic around the annotated methods.  If these
methods were to be called from _inside_ the same class, that would bypass the proxy's
caching logic.  The same warning applies when calling `@Transactional` logic from inside
the same class.

### Fine-grained Caching Control

For fine-grained Redis caching control, a `StringRedisTemplate` is injected,
which handles caching `String` keys to `String` values.  For simple caching cases,
the annotations above are sufficient.  But for finer control over the caching, the
method controlling paginated employee data uses the Redis template object.  It
constructs its own key, cache population, and invalidation.  It returns a
`Page<EmployeeDTO>` object.  It has a 15-minute time-to-live (TTL).

The Redis cache uses a ZSET, which is a Redis sorted set data type.  The method serving
pages of employees uses a key that includes a version number, the sort field, and the
sort direction.  Whenever a change occurs in the employees, the version number is bumped,
so that future requests will establish the new cached entries.  Old cache entries are
ignored, and they eventually expire and get removed.

A reading method must get the version number first, before accessing the cache, so that
it doesn't conflict with a concurrent write operation.

The methods that change the employee list invalidate the cache by bumping the version
number.  Because the updates occur within a transaction, the bump occurs by registering
a quick callback method that bumps the index version immediately following the commit.

## PromotionHarnessService

The `PromotionHarnessService` Service class is a minimal AI Harness that wraps calls
to an LLM.  The LLM has access to any deterministic Tools passed in, which guide it in
creating its free-form text response.

### Spring AI Dependencies

Spring AI is used for communication with an LLM, having its own BOM (bill of materials)
separate from Spring Boot, `org.springframework.ai:spring-ai-bom:2.0.2`.  The actual
dependency itself depends on the model provider, e.g. Anthropic, OpenAI, or Ollama.
Because this project uses a locally installed Ollama model, the dependency here is
`spring-ai-starter-model-ollama`.  Other starter dependencies are available for major
model providers.

Only depend on one chat model starter.  Each starter defines its own `ChatModel` bean,
but Spring AI defines only one `ChatClient.Builder` bean, which needs exactly one
`ChatModel`.  With more than one chat model starter on the classpath, Spring can't
choose which `ChatModel` to give the builder, and creating the builder fails with a
`NoUniqueBeanDefinitionException`.  The failure surfaces when a class such as
`PromotionHarnessService` asks for the injected `ChatClient.Builder`.  To use more than
one provider anyway, mark one `ChatModel` bean `@Primary`, or skip the auto-configured
builder by injecting a specific model and calling `ChatClient.builder(model)`.

### Configuration

Because this application uses a local Ollama model, it's configured as follows:

```properties
spring.ai.ollama.chat.enabled=true
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=qwen2.5:14b
```

E.g., if an Anthropic model were used:

```properties
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}
spring.ai.anthropic.chat.options.model=claude-haiku-4-5
```

Don't leak sensitive information such as an API key!  Use an environment variable.

### Tools

A _tool_ in Spring AI is any functionality that deterministically executes business
logic that an LLM calls.  The class `PromotionRequestTools` is annotated with
`@Component`, a normal Spring Framework annotation.

Any methods exposed as tools to an LLM are annotated with `@Tool(description)`.  The
description is important because the LLM uses it to decide which tool to call based on its
own prompt.  The model used must support tool calling.

Here one `@Tool` method takes a `PromotionStatusRequest`, a Java record containing
data for the promotion status request for an employee.  Each field is annotated with
Jackson annotations:
- `@JsonProperty(required = true)` - A required attribute.
- `@JsonPropertyDescription("The unique employee ID")` - Necessary for the LLM to
decide what values to pass as which fields in the request object.

It returns a `PromotionStatusResponse`, another Java record containing data for the
promotion status response.  Its fields also have the above annotations.  The
descriptions here guide the LLM in its interpretation of the response.

The tool itself mocks up reasons by randomly approving or denying the promotion
request, and coming up with a random reason why in case of denial.  A production tool
would not be so limited; it could call other systems, databases, or any other logic to
perform its purpose.

### Execution

The `PromotionHarnessService` has a Spring AI `ChatClient.Builder` object and a
`PromotionRequestTools` object injected.  It builds the `ChatClient`.  When a prompt
is passed in, a method passes the prompt to the LLM with the `ChatClient`.  The LLM
determines that it can't respond appropriately to the prompt by itself, and requests a
tool call.  Spring AI executes the tool and gives its response back to the LLM, which
uses the tool response to generate its own response.

This AI harness is quite minimal, supplying one Tool that has minimal validation.
More robust AI harnesses might execute LLM actions in a loop, access other systems,
implement RAG (Retrieval Augmented Generation) for the LLM, implement more robust
guardrails, and enforce strict permissions, sandboxing of the LLM, and agent
coordination.

Because LLMs are non-deterministic, the LLM may decide not to request the tool call.  A
proper AI harness should detect that case and implement a retrying mechanism.

### Mocking the LLM

If it isn't desired to run an LLM locally or to pay for a remote LLM service, change the
`PromotionHarnessService` constructor to comment out the building of the `ChatClient` and
instead uncomment the line that constructs a mock `ChatClient` using `ChatClientMockups`.

```java
this.client = new ChatClientMockups.MockChatClient(prTools);
```

This mockup splits the input into 4 space-separated fields, e.g.,
`Employee 2 title Senior Staff Engineer`.  Note that this prompt differs from the prompt
given to an actual LLM, which is described at the bottom of [Controller](controller.md)
that describes the `emp/promotion` endpoint.

It only validates that the second fields is an integer, and anything past the third space
is interpreted as the title, even if more spaces occur.  If the prompt is not validated,
it will respond back with an error.  If the prompt is validated, then it will always call
the tool directly.