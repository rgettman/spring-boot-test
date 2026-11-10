package org.example.springboottest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Use @SpringBootApplication which covers 3 other annotations:
 * - @SpringBootConfiguration - Spring Boot application configuration, allows
 *     registering beans with @Bean.
 * - @EnableAutoConfiguration - autoconfigure beans based on classpath
 *     dependencies.
 * - @ComponentScan - auto-discover/register @Component, @Service, @Repository,
 *     and @RestController.
 * Restrict the base packages for scanning by supplying an array of packages in
 * the scanBasePackages element.
 * Restrict autoconfiguration by passing an array of class objects to the
 * exclude element or fully-qualified class name strings to the excludeName
 * element.
 */
@SpringBootApplication
@EnableCaching
public class Main {
    static void main() {
        // Just do it.
        SpringApplication.run(Main.class);
    }
}
