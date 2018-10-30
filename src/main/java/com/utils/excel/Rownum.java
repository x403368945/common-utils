package com.utils.excel;

import java.util.Arrays;

/**
 * 定义行号获取和自增规则；由于在匿名内部类或stream操作时，直接使用int值无法进行增量操作，故将值和增量方法定义类属性和行为
 *
 * @author Jason Xie on 2017/10/22.
 */
public class Rownum {
    private Rownum(int rownum) {
        this.rownum = rownum;
    }

    public static Rownum ofOne() {
        return new Rownum(1); // rownum 从第一行开始
    }

    public static Rownum of(int rownum) {
        return new Rownum(rownum);
    }

    /**
     * rownum 初始行值1，rowIndex 初始值为 0
     */
    private int rownum;

    /**
     * 获取行号并执行自增量操作
     *
     * @return {@link Rownum}
     */
    public Rownum next() {
        ++rownum;
        return this;
    }
    /**
     * 指定 rownum 值
     *
     * @return {@link Rownum}
     */
    public Rownum set(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("value 不能小于0");
        }
        this.rownum = value;
        return this;
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
    public int index() {
        return rownum - 1;
    }

    public static void main(String[] args) {
        final Rownum rownum = Rownum.of(0);
        for (int i = 0; i < 10; i++) {
            System.out.println(Arrays.asList(rownum.next().get(), rownum.index()));
        }
    }
}
