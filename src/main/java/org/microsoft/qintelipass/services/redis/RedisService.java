package org.microsoft.qintelipass.services.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void setValue(String key, String value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 检查 key 是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 获取 key 的剩余过期时间（秒），若 key 不存在或未设置过期时间返回 0
     */
    public long getExpire(String key) {
        Long expire = redisTemplate.getExpire(key);
        return expire != null && expire > 0 ? expire : 0;
    }

    /**
     * 设置 key 的过期时间
     */
    public void setValue(String key, String value, long seconds) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(seconds));
    }
}
