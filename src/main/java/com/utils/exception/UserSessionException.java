package com.utils.exception;

/**
 * 自定义异常:用户会话异常，会话不存在
 * @author Jason Xie 2016-11-23 
 *
 */
public class UserSessionException extends RuntimeException {
	public UserSessionException() {
		super();
	}

	public UserSessionException(String msg) {
		super(msg);
	}

	public UserSessionException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public UserSessionException(Throwable cause) {
		super(cause);
	}

}
