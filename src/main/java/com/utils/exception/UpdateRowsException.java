package com.utils.exception;

import java.util.Objects;

/**
 * 自定义异常: 数据更新异常
 * @author Jason Xie 2016-11-23 
 *
 */
public class UpdateRowsException extends RuntimeException {

	/**
	 * 断言是否抛出数据更新异常，数据变更行数必须为1
	 * @param rows Number 影响行数
	 */
	public static void asserts(Number rows) {
		if (1 != rows.longValue()) throw new UpdateRowsException("数据变更失败，影响行数:".concat(Objects.toString(rows.intValue())));
	}

	public UpdateRowsException() {
		super();
	}

	public UpdateRowsException(String msg) {
		super(msg);
	}

	public UpdateRowsException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public UpdateRowsException(Throwable cause) {
		super(cause);
	}
}
