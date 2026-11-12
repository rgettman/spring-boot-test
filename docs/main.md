# Main

The `Main` class starts the Spring Boot application.  It has the following Spring
Boot annotations:
- `@SpringBootApplication` - Encompasses 3 other Spring Boot annotations:
  - `@SpringBootConfiguration` - Spring Boot application configuration, allows
    registering beans with `@Bean`.
  - `@EnableAutoConfiguration` - Autoconfigure beans based on classpath dependencies.
  - `@ComponentScan` - auto-discover/register `@Component`, `@Service`, and
    `@Repository` beans.  A `@RestController` is covered under `@Component`.  By
    default, it scans `Main`'s package and all subpackages.
- `@EnableCaching` - Allows Spring to intercept methods annotated with special caching
  annotations.  Here, [Redis](redis.md) is configured as the caching provider.
  Annotations that utilize caching are covered by the [Service](service.md) documentation.

Java 25 allows the `main` method not to be `public`, unlike previous Java versions.

All it needs to call is `SpringApplication.run(Main.class, args);` and Spring does a lot of
heavy lifting to start the Spring Boot application:

- Creates the Application Context, which is the Spring Container that holds all beans.
Handles all instantiation and injection.
- Runs auto-configuration.
- Starts an embedded web server, configured here to run on port 8080.