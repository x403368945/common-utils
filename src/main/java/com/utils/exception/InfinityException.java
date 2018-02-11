package com.utils.exception;

import javax.script.ScriptException;

/**
 * 自定义异常：js执行算术表达式出现除数为0时抛出异常
 * @author Jason Xie 2017年5月10日
 */
public class InfinityException extends ScriptException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8760911634666797786L;

	public InfinityException(Exception e) {
		super(e);
	}

	public InfinityException(String s) {
		super(s);
	}

}
