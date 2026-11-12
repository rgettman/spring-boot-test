# Redis

Redis is used for caching employees.  This application is configured to connect to a
local Redis server, but it can be configured to connect to any Redis server on the
network.

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

## Config

The `RedisConfig` class tells Redis how to serialize and deserialize the objects to
cache.  The `@Configuration` annotation on the class means that Spring considers the
class to be a source of Beans.  Spring creates a proxy subclass, so this class cannot
be `final`.

With such classes, methods annotated with `@Bean` are called and the  returned object
is placed in the Spring Container for later use.  By default, it is a singleton - only
one instance is instantiated.  To use a different scope other than singleton, add
the `@Scope` annotation to the method also.

- `@Scope("prototype")` - A new bean returned each time.
- `@Scope("request")` - A single bean for each incoming HTTP request.
- `@Scope("session")` or `@SessionScope` - A single bean for each user session.
Spring keeps track of a session with a `JSESSIONID` cookie.

Here, this class configures a `RedisCacheManager` bean.