package com.utils.exception;

/**
 * 自定义异常: 数据更新异常
 *
 * @author Jason Xie 2016-11-23
 */
public class UpdateRowsException extends RuntimeException {

    /**
     * 断言是否抛出数据更新异常，数据变更行数必须为1
     *
     * @param rows long 影响行数
     */
    public static void asserts(long rows) {
        if (1 != rows) throw new UpdateRowsException(String.format("数据变更失败，影响行数:%d", rows));
    }

    public static void asserts(int rows) {
        asserts((long) rows);
    }
    /**
     * 断言是否为批量更新
     * 断言是否抛出批量更新异常，数据变更行数必须大于0
     *
     * @param rows long 影响行数
     */
    public static void batch(long rows) {
        if (rows > 0) throw new UpdateRowsException(String.format("批量数据变更失败，影响行数:%d", rows));
    }

    public static void batch(int rows) {
        batch((long) rows);
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
