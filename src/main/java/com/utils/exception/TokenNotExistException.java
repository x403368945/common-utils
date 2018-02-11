package com.utils.exception;

/**
 * 自定义异常: token 不存在
 * @author Jason Xie 2016-11-23 
 *
 */
public class TokenNotExistException extends RuntimeException {
	public TokenNotExistException(String msg) {
		super(msg);
	}
}
