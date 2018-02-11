package com.utils.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.utils.common.entity.base.ItemInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 构建Map对象，支持链式请求
 * Created by Jason Xie on 2017-9-30
 */
@Slf4j
public class MapBuilder<K, V> {
    private final Map<K, V> values;

    public MapBuilder() {
        this(null);
    }

    public MapBuilder(Map<K, V> values) {
        this.values = Objects.isNull(values) ? new LinkedHashMap<>() : values;
    }

/* // 方案二 实现

    private MapBuilder(Map<K, V> values) {
        this.values = Objects.isNull(values) ? new HashMap<>() : values;
    }

    public static <K, V> MapBuilder<K, V> builder(Map<K, V> values) {
        return new MapBuilder<K, V>(values);
    }

    public static <K, V> MapBuilder<K, V> builder() {
        return new MapBuilder<K, V>(new HashMap<K, V>());
    }
*/
    public MapBuilder<K, V> put(K key, V value) {
        if (Objects.nonNull(key) && Objects.nonNull(value)) this.values.put(key, value);
        return this;
    }
    public MapBuilder<K, V> putAll(Map<K, V> values) {
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
        return new MapBuilder<String, Object>().put("params", json()).build();
    }
//    public Row buildRow() {
//        return (Row) this.values;
//    }

    public String json() {
        return JSON.toJSONString(this.values);
    }

    public static void main(String[] args) {
        { // 方案一demo
            log.debug(JSON.toJSONString(
                    new MapBuilder<String, String>()
                            .put("111", "Jason")
                            .put("222", "Jack")
                            .build()
            ));
            log.debug(JSON.toJSONString(
                    new MapBuilder<Integer, String>()
                            .put(1, "Jason")
                            .put(2, "Jack")
                            .build()
            ));
            log.debug(JSON.toJSONString(
                    new MapBuilder<Integer, ItemInfo>()
                            .put(1, ItemInfo.builder().label("name").value("Jason").build())
                            .put(2, ItemInfo.builder().label("name").value("Jack").build())
                            .build()
            ));
            log.debug(JSON.toJSONString(
                    new MapBuilder<Integer, ItemInfo>()
                            .putAll(new MapBuilder<Integer, ItemInfo>()
                                    .put(3, ItemInfo.builder().label("name").value("Jason").build())
                                    .put(4, ItemInfo.builder().label("name").value("Jack").build())
                                    .build())
                            .putAll(null)
                            .put(1, ItemInfo.builder().label("name").value("Jason").build())
                            .put(2, ItemInfo.builder().label("name").value("Jack").build())
                            .build()
            ));
            MapBuilder<Integer, ItemInfo> builder = new MapBuilder<>();
            for (int i = 0; i < 5; i++) {
                builder.put(i, ItemInfo.builder().label("value"+i).value(i).build());
            }
            log.debug(builder.json());
        }

        { // 方案二 demo
//            log.debug(MapBuilder.builder().put("1", "Jason").put("2", "Jack").json());
//            log.debug(MapBuilder.builder(new HashMap<String, String>()).put("1", "Jason").put("2", "Jack").json());
//            log.debug(MapBuilder.builder(new HashMap<Integer, ItemInfo>()).put(1, new ItemInfo("name", "Jason")).put(2, new ItemInfo("name", "Jack")).json());
        }
    }

}
