package com.utils.common.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;


/**
 * 数据模型扩展
 *
 * @author Jason Xie 2017-9-26
 */
public interface IJson {

    /**
     * 当前类转换为Json字符串
     * @return String
     */
    @JSONField(serialize = false, deserialize = false)
    default String json(SerializerFeature... features) {
        return JSON.toJSONString(this, features);
    }

    /**
     * 当前类转换为格式化后的Json字符串
     * @return String
     */
    @JSONField(serialize = false, deserialize = false)
    default String jsonFormat() {
        return json(SerializerFeature.PrettyFormat);
    }
    /**
     * 当前类转换为 JSONObject 对象
     * @return JSONObject
     */
    @JSONField(serialize = false, deserialize = false)
    default JSONObject jsonObject() {
        return (JSONObject) JSON.toJSON(this);
    }
}