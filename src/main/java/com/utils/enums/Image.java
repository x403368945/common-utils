package com.utils.enums;

import lombok.NonNull;

/**
 * 图片格式定义
 *
 * @author Jason Xie on 2018/2/11.
 */
public enum Image {
    JPEG, PNG;

    public String base64(@NonNull final String base64) {
        return String.format("data:image/%s;base64,", this.name().toLowerCase()).concat(base64);
    }
}