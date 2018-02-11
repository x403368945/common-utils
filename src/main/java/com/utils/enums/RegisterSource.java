package com.utils.enums;


/**
 * 用户注册渠道
 *
 * @author Jason Xie 2017年7月4日 下午5:19:05
 */
public enum RegisterSource {
    /**
     * 管理员指定
     */
    System("管理员指定"),
    /**
     * PC-浏览器注册
     */
    Web("PC-浏览器注册"),
    /**
     * Android App注册
     */
    Android("Android App注册"),
    /**
     * IOS App注册
     */
    Ios("IOS App注册"),
    /**
     * 微信注册
     */
    Wechat("微信注册");
    /**
     * 枚举属性说明
     */
    final String comment;
    RegisterSource(String comment) {
        this.comment = comment;
    }
}