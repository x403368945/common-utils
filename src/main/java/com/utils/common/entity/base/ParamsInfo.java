package com.utils.common.entity.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.utils.exception.ParamsException;
import com.utils.util.Maps;
import com.utils.util.Params;
import com.utils.util.Util;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 参数对象
 *
 * @author Jason Xie 2017-9-20
 */
@Slf4j
@JSONType(orders = {"required", "empty", "array", "params"})
public class ParamsInfo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 6441278102384460156L;

    public ParamsInfo() {
    }

    public ParamsInfo(String params) {
        super();
        this.params = params;
    }

    /**
     * 构造函数
     *
     * @param params   String JSON字符串
     * @param required boolean true：params不能为空，为空时抛出异常，false：params可以为空
     */
    public ParamsInfo(String params, boolean required) {
        super();
        this.params = params;
        this.required = required;
    }

    public ParamsInfo(Object params) {
        super();
        this.params = JSON.toJSONString(params);
    }

    /**
     * 构造函数
     *
     * @param params   String JSON字符串
     * @param required boolean true：params不能为空，为空时抛出异常，false：params可以为空
     */
    public ParamsInfo(Object params, boolean required) {
        super();
        this.params = JSON.toJSONString(params);
        this.required = required;
    }

    /**
     * 前端参数集合
     */
    private String params = null;
    /**
     * params 是否必须（非空），true：必须，false：空
     */
    private boolean required = true;

    public String getParams() {
        return params;
    }

    public ParamsInfo setParams(String params) {
        this.params = params;
        return this;
    }

    public ParamsInfo setParams(Object params) {
        this.params = JSON.toJSONString(params);
        return this;
    }

    /**
     * JSONField(deserialize = false) 禁止前端传required，只能由后端指定
     *
     * @return boolean
     */
    @JSONField(deserialize = false)
    public boolean required() {
        return required;
    }

    public ParamsInfo required(boolean required) {
        this.required = required;
        return this;
    }

    /**
     * 判断 params 是否为空或者为一个空集合
     *
     * @return boolean true：是，false：否
     */
    public boolean isEmpty() {
        return Util.isEmpty(params) || "{}".equals(params) || "[]".equals(params) || "[{}]".equals(params);
    }

    /**
     * 判断 params 是否非空且为数组
     *
     * @return boolean true：是，false：否
     */
    public boolean isArray() {
        return Util.isNotEmpty(params) && params.startsWith("[") && params.endsWith("]");
    }

    /**
     * 将 params 转换为 JSONObject, 若params为空，则返回一个空的JSONObject实例
     *
     * @return JSONObject
     * @throws ParamsException params为空或json格式不标准时抛异常
     */
    public JSONObject parseObject() throws ParamsException {
        if (required) Params.notEmpty(params, "请求参数列表params为空");
        else if (isEmpty()) return new JSONObject();
        try {
            return JSON.parseObject(params);
        } catch (Exception e) {
            log.error("请求参数解析失败，原因：JSON格式错误，或字段数据类型不匹配", e);
            throw new ParamsException("请求参数解析失败，原因：JSON格式错误，或字段数据类型不匹配");
        }
    }

    /**
     * 将params转换为泛型指定的类对象， 若params为空则返回一个空的 <T> 实例
     *
     * @param clazz Class<T>
     * @return <T> 实例
     * @throws ParamsException params为空或json格式不标准时抛异常
     */
    public <T> T parseObject(Class<T> clazz) throws ParamsException {
        if (required) Params.notEmpty(params, "请求参数列表params为空");
        else if (isEmpty()) {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                throw new ParamsException("对象初始化失败", e);
            }
        }
        try {
            return JSON.parseObject(params, clazz);
        } catch (Exception e) {
            log.error("请求参数解析失败，原因：JSON格式错误，或字段数据类型不匹配", e);
            throw new ParamsException("请求参数解析失败，原因：JSON格式错误，或字段数据类型不匹配");
        }
    }

    /**
     * 将params转换为泛型指定的泛型类型对象，若params为空则返回 null
     *
     * @param type 泛型类型对象
     * @return <T> 实例
     * @throws ParamsException params为空或json格式不标准时抛异常
     */
    public <T> T parseObject(TypeReference<T> type) throws ParamsException {
        if (required) Params.notEmpty(params, "请求参数列表params为空");
        else if (isEmpty()) return null;
        try {
            return JSON.parseObject(params, type.getType());
        } catch (Exception e) {
            log.error("请求参数解析失败，原因：JSON格式错误，或字段数据类型不匹配", e);
            throw new ParamsException("请求参数解析失败，原因：JSON格式错误，或字段数据类型不匹配");
        }
    }

    /**
     * 将 params 转换为 JSONArray, 若params为空，则返回一个空的JSONArray实例
     *
     * @param clazz Class<T> 泛型类型对象
     * @throws ParamsException params为空或json格式不标准时抛异常
     */
    public <T> List<T> parseArray(Class<T> clazz) throws ParamsException {
        if (required) Params.notEmpty(params, "请求参数列表params为空");
        else if (isEmpty()) return new ArrayList<>();
        try {
            return JSON.parseArray(params, clazz);
        } catch (Exception e) {
            log.error("请求参数解析失败，原因：JSON格式错误，或字段数据类型不匹配", e);
            throw new ParamsException("请求参数解析失败，原因：JSON格式错误，或字段数据类型不匹配");
        }
    }

    /**
     * 将 params 转换为 JSONArray, 若params为空，则返回一个空的JSONArray实例
     *
     * @return JSONArray
     * @throws ParamsException params为空或json格式不标准时抛异常
     */
    public JSONArray parseArray() throws ParamsException {
        if (required) Params.notEmpty(params, "请求参数列表params为空");
        else if (isEmpty()) return new JSONArray();
        try {
            return JSON.parseArray(params);
        } catch (Exception e) {
            log.error("请求参数解析失败，原因：JSON格式错误，或字段数据类型不匹配", e);
            throw new ParamsException("请求参数解析失败，原因：JSON格式错误，或字段数据类型不匹配");
        }
    }

    @Override
    public String toString() {
        return Maps.ofSO()
                .put("empty", this.isEmpty())
                .put("array", this.isArray())
                .put("required", this.required)
                .put("params",
                        isEmpty() ? null : isArray() ? JSON.parseArray(this.params) : JSON.parseObject(this.params)
                )
                .json();
    }

    public static void main(String[] args) {
        try {
            ResultInfo resultInfo = new ParamsInfo("{\"code\":\"SESSION_TIMEOUT\"}").parseObject(ResultInfo.class);
            log.debug("{}", resultInfo);
            resultInfo = new ParamsInfo("{\"code\":\"FAILURE\"}").parseObject(new TypeReference<ResultInfo<Object>>() {
            });
            log.debug("{}", resultInfo);

            List<String> ids = new ParamsInfo("[\"123\",\"456\"]").parseArray(String.class);
            log.debug(JSON.toJSONString(ids));
            ids = new ParamsInfo("[\"789\",\"456\"]").parseObject(List.class);
            log.debug(JSON.toJSONString(ids));

            log.debug("{}", new ParamsInfo(ItemInfo
                    .builder()
                    .label("选项")
                    .value("10")
                    .build()));
            log.debug("{}", new ParamsInfo("[\"d:\\\\project\"]"));
        } catch (ParamsException e) {
            e.printStackTrace();
        }
    }
}
