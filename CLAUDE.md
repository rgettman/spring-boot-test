# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# SpringBootTest

Spring Boot 4.1.1 / Java 25 REST demo (Employee CRUD at `/emp`) with Spring Data JPA, Spring Security, and Redis caching. Source comments are deliberately tutorial-style; keep that style when adding code.

# Structure

- Main source code is in /src/main/java, parent package org.example.springboottest
- config subpackage - Redis config
- controller - Spring Boot Controller
- dto - All DTO objects
- entity - All entity objects
- exception - All exception classes and global exception handler
- repository - All Repositories
- security - Spring security
- service - All services

`src/test` exists but currently contains no test files.

# Profiles

- inmemory - Run an in-memory Repository
- postgres - Run a Repository on a Postgres database

No default profile is set in `application.properties`, so one must be activated (e.g. `-Dspring.profiles.active=inmemory`) or no `EmployeeRepository` bean exists. Redis is required under both profiles (`localhost:6379`), and `postgres` needs `POSTGRES_DEMO_PASSWORD` set (the datasource URL is `jdbc:postgresql://localhost:5432/demo`; the datasource properties are loaded regardless of profile).

# Commands for Development

- Build the Project (no Maven wrapper; use a system `mvn`)
mvn clean install
- Run the app
mvn spring-boot:run -Dspring-boot.run.profiles=inmemory
- Run all tests
mvn test
- Run a single test
mvn test -Dtest=TheTestClass

# Architecture

Request flow: `EmployeeController` -> `EmployeeService` -> `EmployeeRepository` (interface) -> profile-selected implementation. The service maps `Employee` entities to DTOs via a static `MAPPER`; controllers never see entities.

- **Repository abstraction**: `EmployeeRepository` is a plain interface with two implementations, selected by `@Profile`. `PostgresEmployeeRepository` is a Spring Data `JpaRepository` that also extends `EmployeeRepository`, using `default` methods and `@Query` to adapt the custom method names to JPA. `InMemoryEmployeeRepository` is a seeded list. New repository methods must be added to the interface and both implementations.
- **Two caching mechanisms in the service**:
  1. Spring `@Cacheable`/`@CacheEvict` (enabled by `@EnableCaching` on `Main`) for `getAllEmployees` (cache `allEmployees`) and `getEmployeeById` (cache `employees`, key `#id`). Cache TTL is 5 minutes.
  2. A hand-rolled Redis ZSET index per sort (`employees:index:sortBy:<field>:dir:<dir>`, 15 min TTL) used by `getEmployeesPaginated`: the ZSET holds employee IDs ordered by the sort, the page of IDs is read from Redis, then the rows are fetched with `findAllByIdIn`. `createEmployee` and `updateEmployee` must call `clearAllSortingIndexes()` (deletes `employees:index:sortBy:*`) to invalidate these; any new write path needs the same.
- **`RedisConfig`** defines the `RedisCacheManager` with JSON (Jackson 3, `tools.jackson`) value serializers, with per-cache typed serializers for `employees` (`EmployeeDTO`) and `allEmployees` (`List<EmployeeDTO>`). Caching a new type in a new cache requires adding a serializer here, or it falls back to the `Object` serializer. Cached values are DTOs, so DTOs must stay Jackson-deserializable.
- **Errors**: services throw `ResourceNotFoundException`; `GlobalExceptionHandler` maps exceptions to `ErrorResponse`. `NoResourceFoundException` (unmapped paths such as `/favicon.ico`) is mapped to 404; any other exception type without its own handler falls into the `Exception.class` catch-all and returns 500, so add a specific handler for new framework exceptions that should be 4xx. The controller validates/clamps paging params and whitelists `sortBy` (`lastName`, `age`, `salary`) before it reaches the repository.
- **Security**: `SecurityConfig` disables CSRF and currently permits every request (`anyRequest().permitAll()`); the earlier path-specific `permitAll()` and `anyRequest().authenticated()` lines are commented out. No authentication mechanism (`httpBasic`/`formLogin`) is configured.

# Gotchas

- `maven-compiler-plugin` has `<parameters>true</parameters>`; SpEL cache keys like `#id` depend on it.
- `application.properties` sets `spring.jackson.deserialization.fail-on-unknown-properties=true`, so unknown JSON fields in request bodies are rejected.
- `@Transactional` and cache annotations only work through the Spring proxy: call them from another bean, not via `this`.
- The entry point `Main` uses an instance-less `static void main()` (Java 25).
