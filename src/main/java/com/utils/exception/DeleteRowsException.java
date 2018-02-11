package com.utils.exception;

import java.util.Objects;

/**
 * 自定义异常: 标记删除异常
 * @author Jason Xie 2016-11-23 
 *
 */
public class DeleteRowsException extends RuntimeException {
	/**
	 * 断言是否抛出标记删除异常，数据变更行数必须为1
	 * @param rows Number 影响行数
	 */
	public static void asserts(Number rows) {
		if (1 != rows.longValue()) throw new DeleteRowsException("删除失败，影响行数:".concat(Objects.toString(rows.intValue())));
	}
	/**
	 * 断言是否抛出批量标记删除异常，数据变更行数必须大于1
	 * @param rows Number 影响行数
	 * @param batch boolean 是否为批量删除
	 */
	public static void asserts(Number rows, boolean batch) {
		if (rows.longValue() > 0) throw new DeleteRowsException("删除失败，影响行数:".concat(Objects.toString(rows.intValue())));
	}

	public DeleteRowsException() {
		super();
	}

	public DeleteRowsException(String msg) {
		super(msg);
	}

	public DeleteRowsException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public DeleteRowsException(Throwable cause) {
		super(cause);
	}
}
