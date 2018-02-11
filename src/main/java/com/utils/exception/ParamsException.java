package com.utils.exception;

/**
 * 自定义异常:请求参数异常
 * @author Jason Xie 2016-11-23 
 *
 */
public class ParamsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1021721058068927233L;
	public ParamsException() {
		super();
	}

	public ParamsException(String msg) {
		super(msg);
	}

	public ParamsException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public ParamsException(Throwable cause) {
		super(cause);
	}

}
