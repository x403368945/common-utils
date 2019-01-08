package com.utils.exception;

/**
 * 自定义异常:HTTP 请求异常
 * @author 谢长春 2016-11-23 .
 *
 */
public class HttpException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1021721058068927233L;
	public HttpException() {
		super();
	}

	public HttpException(String msg) {
		super(msg);
	}

	public HttpException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public HttpException(Throwable cause) {
		super(cause);
	}

}
