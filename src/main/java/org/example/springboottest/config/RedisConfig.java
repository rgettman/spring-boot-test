package org.example.springboottest.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.springboottest.dto.EmployeeDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

/**
 * Use @Configuration for a class that supplies @Bean objects.
 * Autoconfiguration picks up this annotation.
 */
@Configuration
public class RedisConfig {
    /**
     * Use @Bean to supply a bean to Spring.
     * This code supposedly tells Redis to serialize data with JSON, instead of
     * requiring entities to be Serializable.
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        // Jackson mapper
        ObjectMapper mapper = new ObjectMapper();

        // Fallback serializer.
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new JacksonJsonRedisSerializer<>(mapper, Object.class)));

        // Create specialized serializers for our types here.
        JacksonJsonRedisSerializer<EmployeeDTO> empSerializer = new JacksonJsonRedisSerializer<>(mapper, EmployeeDTO.class);
        JavaType empListType = mapper.getTypeFactory().constructCollectionType(List.class, EmployeeDTO.class);
        JacksonJsonRedisSerializer<List<EmployeeDTO>> empListSerializer = new JacksonJsonRedisSerializer<>(mapper, empListType);

        // Place them in a map.
        Map<String, RedisCacheConfiguration> specialCacheConfigs = new HashMap<>();
        specialCacheConfigs.put("employees", RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(empSerializer)));
        specialCacheConfigs.put("allEmployees", RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(empListSerializer)));

        // Cache default is default serializer, but add special cache configs.
        return RedisCacheManager.builder(factory).cacheDefaults(config)
                .withInitialCacheConfigurations(specialCacheConfigs)
                .build();
    }
}
