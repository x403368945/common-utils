package com.utils.exception;

import com.utils.ICode;
import com.utils.enums.Code;

/**
 * 自定义异常:指定返回编码异常，禁止指定Code.A00000， Code.A00000 表示成功
 *
 * @author 谢长春 2017年7月21日 下午1:02:04
 */
public class CodeException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private ICode code;

    public CodeException(CodeException e) {
        super(String.format("%s:%s", e.getCode(), e.getMessage()), e);
        this.code = e.getCode();
        if (Code.A00000 == this.code) {
            this.code = Code.A00001;
        }
    }

    public CodeException(ICode code, String message) {
        super(String.format("%s:%s", code.name(), message));
        this.code = code;
        if (Code.A00000 == this.code) {
            this.code = Code.A00001;
        }
    }

    public CodeException(ICode code, String message, Throwable cause) {
        super(String.format("%s:%s", code.name(), message), cause);
        this.code = code;
        if (Code.A00000 == this.code) {
            this.code = Code.A00001;
        }
    }

    public ICode getCode() {
        return code;
    }
}
