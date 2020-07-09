package com.utils;

import com.utils.exception.CodeException;

/**
 * 响应代码枚举接口，所有响应代码扩展都必须实现该接口，包括调用第三方的响应代码
 *
 * @author 谢长春 2020-07-09
 */
public interface ICode {

    /**
     * 枚举名称
     *
     * @return {@link String}
     */
    String name();

    /**
     * 获取响应消息
     *
     * @return {@link String}
     */
    String getComment();

    /**
     * 使用异常代码构造 CodeException
     *
     * @param exception {@link String} 异常消息
     * @return {@link CodeException}
     */
    default CodeException toCodeException(final String exception) {
        return new CodeException(this, exception);
    }

}
