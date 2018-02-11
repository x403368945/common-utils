package com.utils.common.entity.excel;

/**
 * 定义行号获取和自增规则；由于在匿名内部类或stream操作时，直接使用int值无法进行增量操作，故将值和增量方法定义类属性和行为
 *
 * @author Jason Xie on 2017/10/22.
 */
public class Rownum {
    private Rownum(int rownum) {
        this.rownum = rownum;
    }
    public static Rownum create() {
        return new Rownum(1); // rownum 从第一行开始
    }
    public static Rownum of(int rownum) {
        return new Rownum(rownum);
    }

    private int rownum;

    /**
     * 获取行号并执行自增量操作
     *
     * @return int
     */
    public int next() {
        return rownum++;
    }

    /**
     * 获取当前行号
     *
     * @return int
     */
    public int get() {
        return rownum;
    }

    /**
     * 获取行索引
     *
     * @return int
     */
    public int rowIndex() {
        return rownum - 1;
    }
}
