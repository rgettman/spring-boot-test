package org.example.springboottest.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * The @EnableWebSecurity annotation turns on web security filters.
 * The @Configuration annotation means Springs registers beans created by
 * methods annotated with @Bean.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // Could also do csrf -> csrf.ignoringRequestMatchers("api/**")
        // to disable CSRF detection for a set of paths.
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                        //.requestMatchers("/emp", "/emp/**", "/error").permitAll());
                        //.anyRequest().authenticated());
                    .anyRequest().permitAll());
        return http.build();
    }

    // A lot more to create HERE to turn on for authentication.
    // What about authorization?
}
