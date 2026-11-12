# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# SpringBootTest

Spring Boot 4.1.1 / Java 25 REST demo (Employee CRUD at `/emp`) with Spring Data JPA, Spring Security, Redis caching, and a Spring AI (Ollama) promotion-status endpoint. Source comments are deliberately tutorial-style; keep that style when adding code. User-facing docs live in `README.md` and `docs/*.md` (one per area); update them when behavior changes.

# Structure

- Main source code is in /src/main/java, parent package org.example.springboottest
- config subpackage - Redis config
- controller - Spring Boot Controller
- dto - All DTO objects
- entity - All entity objects
- exception - All exception classes and global exception handler
- harness - AI promotion harness: `harness.dto` (tool request/response records) and `harness.tool` (`@Tool` methods)
- repository - All Repositories
- security - Spring security
- service - All services

`src/test` exists but currently contains no test files.

# Profiles

- inmemory - Run an in-memory Repository
- postgres - Run a Repository on a Postgres database

No default profile is set in `application.properties`, so one must be activated (e.g. `-Dspring.profiles.active=inmemory`) or no `EmployeeRepository` bean exists. Redis is required under both profiles (`localhost:6379`), and `postgres` needs `POSTGRES_DEMO_PASSWORD` set (the datasource URL is `jdbc:postgresql://localhost:5432/demo`; the datasource properties are loaded regardless of profile). Ollama (`localhost:11434`, model `qwen2.5:14b`) is only needed for `GET /emp/promotion`.

# Commands for Development

Use the Maven wrapper (`./mvnw`, or `mvnw.cmd` on Windows); no system `mvn` is needed.

- Build the Project
./mvnw clean install
- Run the app
./mvnw spring-boot:run -Dspring-boot.run.profiles=inmemory
- Run all tests
./mvnw test
- Run a single test
./mvnw test -Dtest=TheTestClass

# Architecture

Request flow: `EmployeeController` -> `EmployeeService` -> `EmployeeRepository` (interface) -> profile-selected implementation. The service maps `Employee` entities to DTOs via a static `MAPPER`; controllers never see entities.

- **Repository abstraction**: `EmployeeRepository` is a plain interface with two implementations, selected by `@Profile`. `PostgresEmployeeRepository` is a Spring Data `JpaRepository` that also extends `EmployeeRepository`, using `default` methods and `@Query` to adapt the custom method names to JPA. `InMemoryEmployeeRepository` is a seeded list. New repository methods must be added to the interface and both implementations.
- **Two caching mechanisms in the service**:
  1. Spring `@Cacheable`/`@CacheEvict` (enabled by `@EnableCaching` on `Main`) for `getAllEmployees` (cache `allEmployees`) and `getEmployeeById` (cache `employees`, key `#id`). Cache TTL is 5 minutes.
  2. A hand-rolled Redis ZSET index per sort (`employees:index:v<version>:sortBy:<field>:dir:<dir>`, 15 min TTL) used by `getEmployeesPaginated`: the ZSET holds employee IDs ordered by the sort, the page of IDs is read from Redis, then the rows are fetched with `findAllByIdIn`. The version comes from the counter `employees:index:version`, which readers read first. `createEmployee` and `updateEmployee` must call `invalidateSortingIndexes()`, which bumps the counter in an `afterCommit` callback (not before the commit, or a concurrent reader could rebuild the index from old data); old-version keys are left to expire. Any new write path needs the same.
- **`RedisConfig`** defines the `RedisCacheManager` with JSON (Jackson 3, `tools.jackson`) value serializers, with per-cache typed serializers for `employees` (`EmployeeDTO`) and `allEmployees` (`List<EmployeeDTO>`). Caching a new type in a new cache requires adding a serializer here, or it falls back to the `Object` serializer. Cached values are DTOs, so DTOs must stay Jackson-deserializable.
- **Errors**: services throw `ResourceNotFoundException`; `GlobalExceptionHandler` maps exceptions to `ErrorResponse`. `NoResourceFoundException` (unmapped paths such as `/favicon.ico`) is mapped to 404, and `MethodArgumentNotValidException` (a failed `@Valid` request body) to 400; any other exception type without its own handler falls into the `Exception.class` catch-all and returns 500, so add a specific handler for new framework exceptions that should be 4xx. The controller validates/clamps paging params and whitelists `sortBy` (`lastName`, `age`, `salary`) before it reaches the repository. Request bodies are validated with Bean Validation: `EmployeeCreateDTO` uses `@NotBlank`/`@Positive` (all fields required; `@Positive` also catches omitted primitives, which Jackson defaults to 0), `EmployeePatchDTO` uses `@Pattern`/`@Positive` on nullable fields (not `@NotBlank`, which rejects the null of an omitted PATCH field), and the controller parameters need `@Valid`. A new request DTO needs the same `@Valid` or its constraints are silently ignored.
- **Promotion harness** (`GET /emp/promotion?prompt=...`): `EmployeeController` -> `PromotionHarnessService` -> a `ChatClient` built from the auto-configured `ChatClient.Builder` (system prompt + `PromotionRequestTools` as default tools) -> Ollama. The model must decide to call `checkPromotionStatus`, whose result it then phrases as text. The tool's approval is a random mock, not real data. Spring AI has its own BOM (`spring-ai-bom`, separate from Boot's). Injecting `ChatClient.Builder` only works while a single `ChatModel` starter (Ollama) is on the classpath; the Anthropic starter is commented out in `pom.xml` and its properties in `application.properties`, because each starter defines its own `ChatModel` bean and the single auto-configured builder needs exactly one, so having both fails with `NoUniqueBeanDefinitionException`. To use several, mark one `ChatModel` `@Primary` or inject a specific model and call `ChatClient.builder(model)`. `ChatClientMockups` is a fake `ChatClient` for running without a model (see the commented line in the service constructor).
- **Security**: `SecurityConfig` disables CSRF and currently permits every request (`anyRequest().permitAll()`); the earlier path-specific `permitAll()` and `anyRequest().authenticated()` lines are commented out. No authentication mechanism (`httpBasic`/`formLogin`) is configured.

# Gotchas

- Tool calling needs a model that emits structured tool calls. `qwen2.5-coder:14b` printed the call as plain text (tool never ran, JSON returned as the answer); `qwen2.5:14b` works. Prompt wording matters: "Is employee 2 approved for promotion to Staff Software Engineer" works, while "Employee 2 title Staff Software Engineer" makes the model ask a follow-up question. A missing `Checking promotion status for employee ID ...` log line means the tool was not invoked.

- `maven-compiler-plugin` has `<parameters>true</parameters>`; SpEL cache keys like `#id` depend on it.
- `application.properties` sets `spring.jackson.deserialization.fail-on-unknown-properties=true`, so unknown JSON fields in request bodies are rejected.
- `@Transactional` and cache annotations only work through the Spring proxy: call them from another bean, not via `this`.
- The entry point `Main` uses a non-`public` `static void main(String[] args)` (allowed by Java 25) and passes `args` to `SpringApplication.run`, so command-line arguments such as `--spring.profiles.active=inmemory` take effect.
