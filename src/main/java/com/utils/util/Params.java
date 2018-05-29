package com.utils.util;

import java.util.*;

import static com.utils.enums.Code.PARAMS_ERROR;

/**
 * 警告：此断言异常将会抛参数错误到前端，若异常不是前端传的参数错误，请使用 Asserts 断言类
 * 参数断言，断言不满足则抛出 Code.PARAMS_ERROR 异常；
 * @author Jason Xie on 2017/10/26.
 */
public class Params {
    /**
     * 校验boolean是否为true；若为 false 否则抛出 Code.PARAMS_ERROR 异常
     *
     * @param value boolean
     * @param message String 异常消息内容
     */
    public static void isTrue(final boolean value, final String message) {
        if (!value) throw PARAMS_ERROR.exception(message);
    }
    /**
     * 校验boolean是否为false；若为 true 则抛出 Code.PARAMS_ERROR 异常
     *
     * @param value boolean
     * @param message String 异常消息内容
     */
    public static void isFalse(final boolean value, final String message) {
        if (value) throw PARAMS_ERROR.exception(message);
    }
    /**
     * 校验Collection集合不能为空且长度不能为0；否则抛出 Code.PARAMS_ERROR 异常
     *
     * @param values Collection 集合
     * @param message String 异常消息内容
     */
    public static void notEmpty(final Collection<?> values, final String message) {
        if (Objects.isNull(values) || values.size() == 0) throw PARAMS_ERROR.exception(message);
    }

    /**
     * 校验Map集合不能为空且长度不能为0；否则抛出 Code.PARAMS_ERROR 异常
     *
     * @param values Map 集合
     * @param message String 异常消息内容
     */
    public static void notEmpty(final Map<?, ?> values, final String message) {
        if (Objects.isNull(values) || values.size() == 0) throw PARAMS_ERROR.exception(message);
    }

    /**
     * 校验数组不能为空且长度不能为0；否则抛出 Code.PARAMS_ERROR 异常
     *
     * @param values T[] 数组
     * @param message String 异常消息内容
     */
    public static <T> void notEmpty(final T[] values, final String message) {
        if (Objects.isNull(values) || values.length == 0) throw PARAMS_ERROR.exception(message);
    }

    /**
     * 校验对象不能为空；否则抛出 Code.PARAMS_ERROR 异常
     *
     * @param value Object 判断值
     * @param message String 异常消息内容
     */
    public static void notEmpty(final Object value, final String message) {
        if (Objects.isNull(value)) throw PARAMS_ERROR.exception(message);
    }

    /**
     * 校验字符串不能为空；否则抛出 Code.PARAMS_ERROR 异常
     *
     * @param value String 判断值
     * @param message String 异常消息内容
     */
    public static void notEmpty(final String value, final String message) {
        if (Objects.isNull(value) || value.trim().length() == 0) throw PARAMS_ERROR.exception(message);
    }

    /**
     * 校验数字必须大于0；否则抛出 Code.PARAMS_ERROR 异常
     * @param value Number 判断值
     * @param message String 异常消息内容
     */
    private static void checkNumber(final Number value, final String message) {
        if (value.doubleValue() <= 0) throw PARAMS_ERROR.exception(message);
    }

    public static void main(String[] args) {
        try {
            isTrue(1==1, "断言：结果为 true > 不抛异常");
            isTrue(1!=1, "断言：结果为 false > 抛异常");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            isFalse(1!=1, "断言：结果为 false > 不抛异常");
            isFalse(1==1, "断言：结果为 true > 抛异常");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            checkNumber(1, "断言：结果 必须大于0 > 不抛异常");
            checkNumber(0, "断言：结果 必须大于0 > 抛异常");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            notEmpty("aaa", "断言：结果 字段是否为空 > 不抛异常");
            notEmpty("", "断言：结果 字段是否为空 > 抛异常");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            notEmpty(new HashSet<String>(){{add("Set");}}, "断言：结果 Set 不能为空 > 不抛异常");
            notEmpty(new HashSet<>(), "断言：结果 Set 不能为空 > 抛异常");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            notEmpty(Arrays.asList("a","b"), "断言：结果 ArrayList 不能为空 > 不抛异常");
            notEmpty(new ArrayList<>(), "断言：结果 ArrayList 不能为空 > 抛异常");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
