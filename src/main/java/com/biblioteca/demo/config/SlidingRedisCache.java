package com.biblioteca.demo.config;

import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.cache.RedisCacheWriter.TtlFunction;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

public class SlidingRedisCache extends RedisCache { 

    private final TtlFunction ttlFunction;
    private final RedisConnectionFactory connectionFactory;

    public SlidingRedisCache(
            String name,
            RedisCacheWriter cacheWriter,
            RedisCacheConfiguration cacheConfig,
            RedisConnectionFactory connectionFactory) {
        super(name, cacheWriter, cacheConfig);
        this.ttlFunction = cacheConfig.getTtlFunction();
        this.connectionFactory = connectionFactory;
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper value = super.get(key);

        if (value != null) {
            // 1. Calcula a Duração a partir da TtlFunction
            Duration duration = ttlFunction.getTimeToLive(key, value.get());

            if (duration != null && !duration.isZero() && !duration.isNegative()) {
                // 2. createCacheKey gera a String (ex: "users-dto::deus@gmail.com")
                String cacheKey = createCacheKey(key);
                
                // 3. serializeCacheKey converte a String para byte[] usando o StringRedisSerializer
                byte[] binaryKey = serializeCacheKey(cacheKey);

                // 4. Executa o comando EXPIRE diretamente na conexão
                try (RedisConnection connection = connectionFactory.getConnection()) {
                    connection.keyCommands().expire(binaryKey, duration.getSeconds());
                }
            }
        }

        return value;
    }
}