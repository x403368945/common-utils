package com.utils.exception;

/**
 * 自定义异常: 删除异常
 *
 * @author Jason Xie 2016-11-23
 */
public class DeleteRowsException extends RuntimeException {
    /**
     * 断言是否抛出删除异常，数据变更行数必须为1
     *
     * @param rows long 影响行数
     */
    public static void asserts(long rows) {
        if (1 != rows) throw new DeleteRowsException(String.format("删除失败，影响行数:%d", rows));
    }

    public static void asserts(int rows) {
        asserts((long) rows);
    }

    /**
     * 断言是否为批量删除
     * 断言是否抛出批量删除异常，数据变更行数必须大于0
     *
     * @param rows long 影响行数
     */
    public static void batch(long rows) {
        if (rows > 0) throw new DeleteRowsException(String.format("批量删除失败，影响行数:%d", rows));
    }

    public static void batch(int rows) {
        batch((long) rows);
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
