package com.utils.exception;


/**
 * 自定义异常:
 *
 * @author 谢长春 2017年7月21日 下午1:02:04 .
 */
public class NotFoundException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public NotFoundException() {
        super();
    }

    public NotFoundException(String msg) {
        super(msg);
    }

    public NotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }
}
