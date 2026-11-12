# Spring Boot Test

A test Spring Boot project to manage employees, using JPA/Hibernate, Redis caching,
and Spring AI with Ollama.  Runs locally on port 8080.

Its functionality includes [endpoints](docs/controller.md#endpoints) for listing all
employees, listing employees by page, retrieving a single employee, creating a new
employee, and updating an existing employee.  In addition, there is an endpoint that
accepts a prompt to determine if an employee qualifies for a promotion.  The approval
is a randomly determined mock result.

Sample prompt: "Is employee 2 approved for promotion to Staff Software Engineer"
- `http://localhost:8080/emp/promotion?prompt=Is employee 2 approved for promotion to Staff Software Engineer`

## Dependencies

A Spring Boot application uses the Spring Boot dependency, 

### Maven

In `pom.xml`, use dependency management to define the common version number for not just
the main Spring Boot Starter, but for any other Starter dependencies.

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-dependencies</artifactId>
      <version>4.1.1</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

Then the dependency itself:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Other dependencies for other purposes: JPA, caching, Redis, Postgres, Spring AI
have their own dependencies.

### Gradle

For Gradle users, if this were a Gradle project, then this would be added to
`build.gradle`:

```groovy
plugins {
  id 'org.springframework.boot' version '4.1.1'
}

dependencies {
  implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
  implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

## Profiles
A Spring Boot profile controls which beans or components are active.  Activate a
profile using any of these examples:
- Pass a system property: `java -Dspring.profiles.active=dev,cloud -jar app.jar`
- Command line argument: `java -jar app.jar --spring.profiles.active=dev,cloud`
- Environment variable: `SPRING_PROFILES_ACTIVE=dev,cloud`

The above profiles are listed as examples only.  They are not recognized by this
application.

This application recognizes the following profiles.  Exactly one profile must be active.
- `inmemory` - Maintains the employee list in memory, seeded with a list of 4 employees.
- `postgres` - Queries a Postgres database with an employee table in the demo schema
with columns corresponding to an employee's attributes.

## Prerequisites
- Java 25
- Maven is not necessary.  The included Maven wrapper downloads Maven 3.9.11 when
called for the first time.
- Redis installed on localhost:6379 (configurable).
- For the `postgres` profile: An `employee` [table](docs/repository.md#employee-table)
in the `demo` schema and a `POSTGRES_DEMO_PASSWORD` environment variable.  Postgres
installed at localhost:5432 (configurable).
- Ollama installed, with the `qwen2.5:14b` model pulled (configurable).  It's only used
for the `/emp/promotion` endpoint.

## Security

For now, all requests are permitted.  No authentication or authorization is performed.

## Commands

- Build it: `mvnw clean install`
- Run it: `mvnw spring-boot:run -Dspring-boot.run.profiles=inmemory`

## Documentation
- [Controller](docs/controller.md)
- [Exception Handling](docs/error.md)
- [Main class](docs/main.md)
- [Redis Integration](docs/redis.md)
- [Repository](docs/repository.md)
- [Services](docs/service.md)