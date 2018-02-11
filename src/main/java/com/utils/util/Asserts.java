package com.utils.util;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Objects;

/**
 * 自定义断言类，继承org.springframework.util.Assert
 * @author Jason Xie on 2017/10/26.
 */
public class Asserts extends Assert {
    /**
     * 校验boolean是否为false；若为 false 则抛出 IllegalArgumentException 异常
     *
     * @param value boolean
     * @param message String 异常消息内容
     */
    public static void isFalse(final boolean value, final String message) {
        if (value) throw new IllegalArgumentException(message);
    }

    /**
     * 校验对象不能为空；否则抛出 IllegalArgumentException 异常
     *
     * @param value Object 判断值
     * @param message String 异常消息内容
     */
    public static void notEmpty(final Object value, final String message) {
        if (Objects.isNull(value)) throw new IllegalArgumentException(message);
    }

    /**
     * 校验字符串不能为空；否则抛出 IllegalArgumentException 异常
     *
     * @param value String 判断值
     * @param message String 异常消息内容
     */
    public static void notEmpty(final String value, final String message) {
        Assert.hasText(value, message);
    }

    /**
     * 校验数字必须大于0；否则抛出 IllegalArgumentException 异常
     * @param value Number 判断值
     * @param message String 异常消息内容
     */
    public static void gtZore(final Number value, final String message) {
        if (value.doubleValue() <= 0) throw new IllegalArgumentException(message);
    }

    /**
     * 校验 v1.equals(v2) ；否则抛出 IllegalArgumentException 异常
     * @param v1 Object 参数1
     * @param v2 Object 参数2
     * @param message String 异常消息内容
     */
    public static void equals(final Object v1, final Object v2, final String message){
        if(!Objects.equals(v1, v2)) throw new IllegalArgumentException(message);
    }
    public static void main(String[] args) {
        String nullValue = null;
        String emptyValue="";
        String value="Text";
        {
//                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Assert.hasText <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            try {Asserts.hasText(nullValue, "hasText : message : nullValue");} catch (Exception e) {e.printStackTrace();}
            try {Asserts.hasText(emptyValue, "hasText : message : emptyValue");} catch (Exception e) {e.printStackTrace();}
            try {Asserts.hasText(value, "hasText : message : value");} catch (Exception e) {e.printStackTrace();}
        }
        {
//                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Assert.isInstanceOf <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            try {Asserts.isInstanceOf(String.class, nullValue, "isInstanceOf : message : nullValue");} catch (Exception e) {e.printStackTrace();}
            try {Asserts.isInstanceOf(String.class, emptyValue, "isInstanceOf : message : emptyValue");} catch (Exception e) {e.printStackTrace();}
            try {Asserts.isInstanceOf(String.class, value, "isInstanceOf : message : value");} catch (Exception e) {e.printStackTrace();}
        }
        {
//                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Assert.isNull <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            try {Asserts.isNull(nullValue, "isNull : message : nullValue");} catch (Exception e) {e.printStackTrace();}
            try {Asserts.isNull(emptyValue, "isNull : message : emptyValue");} catch (Exception e) {e.printStackTrace();}
            try {Asserts.isNull(value, "isNull : message : value");} catch (Exception e) {e.printStackTrace();}
        }
        {
//                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Assert.notNull <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            try {Asserts.notNull(nullValue, "notNull : message : nullValue");} catch (Exception e) {e.printStackTrace();}
            try {Asserts.notNull(emptyValue, "notNull : message : emptyValue");} catch (Exception e) {e.printStackTrace();}
            try {Asserts.notNull(value, "notNull : message : value");} catch (Exception e) {e.printStackTrace();}
        }
        try {
            String[] arrs = null;
            Asserts.notEmpty(arrs, "Array is empty");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String[] arrs = new String[]{};
            Asserts.notEmpty(arrs, "Array is empty");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Asserts.notEmpty(new ArrayList<>(), "List is empty");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Asserts.isTrue(true, "true 抛出异常");
            Asserts.isTrue(false, "false 抛出异常");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Asserts.isFalse(false, "false 抛出异常");
            Asserts.isFalse(true, "true 抛出异常");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
