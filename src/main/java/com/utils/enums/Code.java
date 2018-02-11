package com.utils.enums;

import com.utils.common.entity.base.ResultInfo;
import com.utils.exception.CodeException;

/**
 * ResultInfo 返回编码定义
 */
public enum Code {
    SUCCESS("成功"),
    FAILURE("失败"),
    PARAMS_ERROR("请求参数列表为空，或参数非空校验失败"),
    USER_NOT_EXIST("用户不存在"),
    USER_PWD_FAILURE("用户名密码错误"),
    USER_DISABLED("账户已禁用"),
    USER_LOCKED("账户已锁定"),
    USER_EXPIRED("账户已过期或未激活，激活链接已发送至邮箱"),
    USER_CREDENTAILS_EXPIRED("证书已过期"),
    PWD_ERROR("密码错误"),
    EMAIL_EXIST("邮箱已存在"),
    IMAGE_CODE_ERROR("图片验证码输入错误"),
    PHONE_CODE_ERROR("短信验证码输入错误"),
    CLOSED_PERMISSION("无操作权限"),
    NO_PERMISSION("无操作权限"),
    PHONE_EXIST("手机号已被占用"),
    SESSION_TIMEOUT("会话超时"),
    UPLOAD_FILE_IS_EMPTY("上传文件列表为空"),
    FILE_SUBFIX_ERROR("文件格式不支持"),
    DOWNLOAD_PERMISSION("无下载权限"),
    URL_INVALID("激活链接已失效"),
    CHECK_EMAIL("邮件发送失败，请检查邮箱是否正确"),
    COMPUTING("正在计算"),
    ACTIVITY_NOT_FOUND("活动未创建"),
    ;
    /**
     * 枚举属性说明
     */
    public final String comment;
    Code(String comment) {
        this.comment = comment;
    }

    /**
     * 通过 Code 构建 ResultInfo 对象；注：只能构建 ResultInfo<Object>，若要指定泛型，请使用 new ResultInfo<?> 指定泛型
     * @return ResultInfo<Object>
     */
    public <E> ResultInfo<E> toResult() {
        return new ResultInfo<>(this);
    }
    /**
     * 通过 Code 构建 ResultInfo 对象；注：只能构建 ResultInfo<Object>，若要指定泛型，请使用 new ResultInfo<?> 指定泛型
     * @param exception String 异常消息，可选参数，
     * @return ResultInfo<Object>
     */
    public <E> ResultInfo<E> toResult(final String exception) {
        return new ResultInfo<E>(this).setException(exception);
    }

    /**
     * 构造并返回 CodeException
     * @param exception String 异常消息内容
     * @return CodeException
     */
    public CodeException exception(final String exception) {
        return new CodeException(this, exception);
    }
}