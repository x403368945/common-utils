package com.utils.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.utils.common.entity.base.ItemInfo;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 构建Map对象，支持链式请求
 * Created by Jason Xie on 2017-9-30
 */
@Slf4j
public class Maps<K, V> {
    private final Map<K, V> values;

    public static Maps<Object, Object> of() {
        return new Maps<>(new LinkedHashMap<>());
    }
    public static <K, V> Maps<K, V> of(Class<K> key, Class<V> value) {
        return new Maps<>(new LinkedHashMap<K, V>());
    }
    public static Maps<String, Object> ofSO() {
        return new Maps<>(new LinkedHashMap<String, Object>());
    }
    public static Maps<String, String> ofSS() {
        return new Maps<>(new LinkedHashMap<String, String>());
    }
    public static Map<Object, Object> by(@NonNull final Object key, @NonNull final Object value) {
        return new HashMap<Object, Object>() {{
            put(key, value);
        }};
    }
    public static Map<String, Object> bySO(@NonNull final String key, @NonNull final Object value) {
        return new HashMap<String, Object>() {{
            put(key, value);
        }};
    }
    public static Map<String, String> bySS(@NonNull final String key, @NonNull final String value) {
        return new HashMap<String, String>() {{
            put(key, value);
        }};
    }
    private Maps() {
        this(null);
    }

    public Maps(Map<K, V> values) {
        this.values = Objects.isNull(values) ? new LinkedHashMap<>() : values;
    }

    public Maps<K, V> put(K key, V value) {
        if (Objects.nonNull(key) && Objects.nonNull(value)) this.values.put(key, value);
        return this;
    }
    public Maps<K, V> put(boolean hasTrue, K key, V value) {
        if (hasTrue && Objects.nonNull(key) && Objects.nonNull(value)) this.values.put(key, value);
        return this;
    }
    public Maps<K, V> putAll(Map<K, V> values) {
        if (Objects.nonNull(values)) this.values.putAll(values);
        return this;
    }

    public Map<K, V> build() {
        return this.values;
    }
    public JSONObject buildJSONObject() {
        return (JSONObject) JSONObject.toJSON(this.values);
    }
    public Map<String, Object> buildRoundParams() {
        return Maps.bySO("params", json());
    }

    public String json(SerializerFeature... features) {
        return JSON.toJSONString(this.values, features);
    }

    public static void main(String[] args) {
        { // demo
            log.debug(
                    Maps.ofSS()
                            .put("111", "Jason")
                            .put("222", "Jack")
                            .json(SerializerFeature.PrettyFormat)
            );
            log.debug(
                    Maps.of(Integer.class, String.class)
                            .put(1, "Jason")
                            .put(2, "Jack")
                            .json(SerializerFeature.PrettyFormat)
            );
            log.debug(
                    Maps.of(Integer.class, ItemInfo.class)
                            .put(1, ItemInfo.builder().label("name").value("Jason").build())
                            .put(2, ItemInfo.builder().label("name").value("Jack").build())
                            .json(SerializerFeature.PrettyFormat)
            );
            log.debug(
                    Maps.of(Integer.class, ItemInfo.class)
                            .putAll(Maps.of(Integer.class, ItemInfo.class)
                                    .put(3, ItemInfo.builder().label("name").value("Jason").build())
                                    .put(4, ItemInfo.builder().label("name").value("Jack").build())
                                    .build())
                            .putAll(null)
                            .put(1, ItemInfo.builder().label("name").value("Jason").build())
                            .put(2, ItemInfo.builder().label("name").value("Jack").build())
                            .json(SerializerFeature.PrettyFormat)
            );
            Maps<Integer, ItemInfo> builder = Maps.of(Integer.class, ItemInfo.class);
            for (int i = 0; i < 5; i++) {
                builder.put(i, ItemInfo.builder().label("value"+i).value(i).build());
            }
            log.debug(builder.json());
        }

        System.out.println(Maps.ofSO()
                .put("key", "测试")
                .buildJSONObject());
        System.out.println(Maps.ofSS()
                .put("key", "测试")
                .buildJSONObject());
        System.out.println(Maps.of(Integer.class, String.class)
                .put(1, "测试")
                .buildJSONObject());
        System.out.println(Maps.of(Long.class, String.class)
                .put(1L, "测试")
                .buildJSONObject());
    }

}
