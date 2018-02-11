package com.utils.enums;

import com.utils.util.Util;

/**
 * 选择状态，只有两种状态，YES or NO
 *
 * @author Jason Xie 2017年7月4日 下午5:19:22
 */
public enum Switch {
    /**
     * 否|未读|待处理|下架|未验证|未完成|不支持|未确认|无效
     */
    NO("否|未读|待处理|下架|未验证|未完成|不支持|未确认|无效|未过期（已激活）|暂停"),
    /**
     * 是|已读|已处理|上架|已验证|已完成|	支持 |已确认|有效
     */
    YES("是|已读|已处理|上架|已验证|已完成| 支持|已确认|有效|已过期（未激活）|启动");
    /**
     * 枚举属性说明
     */
    final String comment;

    Switch(String comment) {
        this.comment = comment;
    }

    public int value() {
        return this.ordinal();
    }

    public static Switch valueOf(Integer value) {
        return Util.isEmpty(value) ? null : Switch.values()[value];
    }

}
