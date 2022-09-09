package org.tool.cn.utils.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, String> valueOperations;
    private final HashOperations<String, String, Object> hashOperations;
    private final ListOperations<String, Object> listOperations;
    private final SetOperations<String, Object> setOperations;
    private final ZSetOperations<String, Object> zSetOperations;
    /**  默认过期时长，单位：秒 */
//    public final static long DEFAULT_EXPIRE = 60 * 60 * 24;
    /**
     * 不设置过期时长
     */
    public final static long NOT_EXPIRE = -1;

    @Autowired
    public RedisUtils(RedisTemplate<String, Object> redisTemplate, ValueOperations<String, String> valueOperations, HashOperations<String, String, Object> hashOperations, ListOperations<String, Object> listOperations, SetOperations<String, Object> setOperations, ZSetOperations<String, Object> zSetOperations) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = valueOperations;
        this.hashOperations = hashOperations;
        this.listOperations = listOperations;
        this.setOperations = setOperations;
        this.zSetOperations = zSetOperations;
    }

    /**
     * 设置缓存
     *
     * @param key
     * @param value
     * @param expire 单位: 秒/s
     */
    public void set(String key, Object value, long expire) {
        valueOperations.set(key, toJson(value));
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);

            SetOperations<String, Object> set = redisTemplate.opsForSet();
        }
    }

    public void sadd(String setName, String value){
        setOperations.add(setName,value);
    }

    public void sremove(String setName, String value){
        setOperations.remove(setName,value);
    }

    public void set(String key, Object value) {
        set(key, value, NOT_EXPIRE);
    }

    public Boolean setNx(String key, String value) {
        return valueOperations.setIfAbsent(key, value);
    }

    public Boolean setNx(String key, String value, long expire) {
        return valueOperations.setIfAbsent(key, value, expire, TimeUnit.SECONDS);
    }

    public <T> T get(String key, Class<T> clazz, long expire) {
        String value = valueOperations.get(key);
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
        return value == null ? null : JSONObject.parseObject(value, clazz);
    }

    public <T> T get(String key, Class<T> clazz) {
        return get(key, clazz, NOT_EXPIRE);
    }

    public String get(String key, long expire) {
        String value = valueOperations.get(key);
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
        return value;
    }

    public String get(String key) {
        return get(key, NOT_EXPIRE);
    }

    public String getKey(String key) {
        return valueOperations.get(key);
    }

    public String getAndSet(String key, Object value, long expire) {
        String oldValue = valueOperations.getAndSet(key, toJson(value));
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
        return oldValue;
    }

    public String getAndSet(String key, Object value) {
        return getAndSet(key, value, NOT_EXPIRE);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void delete(Set<String> keys) {
        redisTemplate.delete(keys);
    }


    public Set<String> getKeys(String reg) {
        return redisTemplate.keys(reg);
    }

    /**
     * Object转成JSON数据
     */
    private String toJson(Object object) {
        if (object instanceof Integer || object instanceof Long || object instanceof Float ||
                object instanceof Double || object instanceof Boolean || object instanceof String) {
            return String.valueOf(object);
        }
        return JSON.toJSONString(object);
    }

    public Optional<Long> incr(String key) {
        return Optional.ofNullable(valueOperations.increment(key));
    }

    /**
     * @param key   key
     * @param value 增量
     * @return optional 可能为null
     */
    public Optional<Long> incr(String key, long value) {
        return Optional.ofNullable(valueOperations.increment(key, value));
    }

    public Optional<Long> incrEx(String key, long time) {
        Optional<Long> increment = incr(key);
        Optional<Boolean> expire = expire(key, time);
        return (expire.isPresent() && expire.get()) ? increment : Optional.empty();
    }

    public Optional<Long> incrLoginCount(String key, long time) {
        expire(key, time);
        return incr(key, 1L);
    }

    public Optional<Boolean> expire(String key, long time) {
        return Optional.ofNullable(redisTemplate.expire(key, time, TimeUnit.SECONDS));
    }

    public Long ttl(String key) {
        return redisTemplate.getExpire(key, TimeUnit.MINUTES);
    }

    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public Object hget(String key, String hk) {
        return hashOperations.get(key, hk);
    }

    public void hset(String key, String hk, Object value) {
        hashOperations.put(key, hk, value);
    }

    public Map<String, Object> hmget(String key) {
        return hashOperations.entries(key);
    }

    public void hmset(String key, Map<String, Object> hkValueMap) {
        hashOperations.putAll(key, hkValueMap);
    }

    public void hincr(String key, String hk) {
        hashOperations.increment(key, hk, 1);
    }

    public Set<String> hkeys(String key) {
        return hashOperations.keys(key);
    }

    public void rpush(String key, Object value) {
        listOperations.rightPush(key, value);
    }

    public void rpush(String key, Object... values) {
        listOperations.rightPushAll(key, values);
    }

    public List<Object> range(String key, long start, long end) {
        return listOperations.range(key, start, end);
    }

    public void trim(String key, long start, long end) {
        listOperations.trim(key, start, end);
    }

    /** Add value to a sorted set at key, or update its score if it already exists.
     * @param key – must not be null.
     * @param value – the value.
     * @param score – the score.
     * @return
     */
    public Boolean zsetAdd(String key,
                           Object value,
                           double score) {
        return zSetOperations.add(key, value, score);
    }

    /**
     * Get the cardinality (number of elements) of the sorted set, or 0 if key does not exist.
     * @param key – must not be null.
     * @return null when used in pipeline / transaction.
     */
    public Long zCard(String key) {
        return zSetOperations.zCard(key);
    }

    public Long sSetAdd(String key, Object... value) {
        return setOperations.add(key, value);
    }

    public Long sSetAddEx(long time, String key, Object... value) {
        Long count = setOperations.add(key, value);
        expire(key, time);
        return count;
    }

    public Set<Object> sSetMembers(String key) {
        return setOperations.members(key);
    }

    public Long sSetCard(String key) {
        return setOperations.size(key);
    }
}
