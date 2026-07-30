package org.microsoft.qintelipass.services;

import org.springframework.data.redis.core.RedisTemplate;

public interface IRedisService<T> {
    void setValue(String key, T value);
    T getValue(String key);
    void deleteValue(String key);
    RedisTemplate<String, T> getRedisTemplate();
}