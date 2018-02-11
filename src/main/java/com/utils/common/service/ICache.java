package com.utils.common.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口基础方法定义
 * @author Jason Xie on 2018/2/8.
 */
public interface ICache<V> {
    /**
     * Redis 数据库信息
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    class DBInfo {
        public static DBInfo of(int index, String name, String comment) {
            return DBInfo.builder()
                    .index(index)
                    .name(name)
                    .comment(comment)
                    .build();
        }
        /**
         * 数据库索引
         */
        private int index;
        /**
         * 数据库名称
         */
        private String name;
        /**
         * 数据库说明
         */
        private String comment;
        /**
         * 数据库服务类
         */
        private String clazz;
    }
    /**
     *  获取数据库定义
     * @return {@link DBInfo}
     */
    DBInfo getDB();
    /**
     * 获取 Redis 操作模板
     * @return RedisTemplate
     */
    RedisTemplate<String, V> getRedisTemplate();
    /**
     * 初始化数据
     */
    default void init() {
        setDB();
    }
    /**
     * 初始化数据
     */
    default void setDB() {
        DBInfo db = getDB();
        BoundHashOperations<String, Object, Object> hash = getRedisTemplate().boundHashOps("db");
        hash.put("index", Objects.toString(db.getIndex()));
        hash.put("name", db.getName());
        hash.put("comment", db.getComment());
        hash.put("clazz", db.getClazz());
    }
    /**
     * 检测 key 是否存在
     * @param key String
     * @return boolean,true存在，false不存在
     */
    default boolean hasKey(final String key) {
        return getRedisTemplate().hasKey(key);
    }

    /**
     * 设置过期时间
     * @param key String
     * @param timeout long
     * @param unit TimeUnit
     * @return boolean
     */
    default boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return getRedisTemplate().expire(key, timeout, unit);
    }
    /**
     * 设置过期时间
     * @param key String
     * @param date Date
     * @return boolean
     */
    default boolean expireAt(final String key, final Date date) {
        return getRedisTemplate().expireAt(key, date);
    }

    /**
     * 删除缓存
     * @param keys String[]
     */
    default void delete(final String... keys) {
        getRedisTemplate().delete(Arrays.asList(keys));
    }
    /**
     * 清除数据库
     */
    default void clear() {
        getRedisTemplate().execute(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.flushDb();
                return true;
            }
        });
    }
    /**
     * 检测缓存是否需要刷新
     * @return boolean true需要刷新，false不需要刷新
     */
    default boolean hasRefresh() {
        return !getRedisTemplate().opsForHash().hasKey("db","status");
    }

    /**
     * 设置缓存刷新完成
     */
    default void refreshEnd() {
        getRedisTemplate().boundHashOps("db").put("status", "true");
    }
}
