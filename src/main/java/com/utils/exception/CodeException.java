package com.utils.exception;


import com.utils.enums.Code;

/**
 * 自定义异常:指定返回编码异常,禁止指定Code.SUCCESS
 *
 * @author Jason Xie 2017年7月21日 下午1:02:04
 */
public class CodeException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Code code;

    public CodeException(CodeException e) {
        super(e.getMessage(), e);
        this.code = e.getCode();
        if (Code.SUCCESS == this.code) {
            this.code = Code.FAILURE;
        }
    }

    public CodeException(Code code, String message) {
        super(message);
        this.code = code;
        if (Code.SUCCESS == this.code) {
            this.code = Code.FAILURE;
        }
    }

    public CodeException(Code code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        if (Code.SUCCESS == this.code) {
            this.code = Code.FAILURE;
        }
    }

    public Code getCode() {
        return code;
    }

}
