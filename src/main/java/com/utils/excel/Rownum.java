package com.utils.excel;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定义行号获取和自增规则；由于在匿名内部类或stream操作时，直接使用int值无法进行增量操作，故将值和增量方法定义类属性和行为
 *
 * @author 谢长春 on 2017/10/22 .
 */
public final class Rownum {
    private Rownum(int rownum) {
        this.rownum.set(rownum);
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
    private AtomicInteger rownum = new AtomicInteger(1);

    /**
     * 获取行号并执行自增量操作
     *
     * @return {@link Rownum}
     */
    public Rownum next() {
        rownum.incrementAndGet();
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
        this.rownum.set(value);
        return this;
    }

    /**
     * 获取当前行号
     *
     * @return int
     */
    public int get() {
        return rownum.get();
    }
    /**
     * 返回当前行号，然后行号 +1
     *
     * @return int
     */
    public int poll() {
        return rownum.getAndIncrement();
    }

    /**
     * 获取行索引
     *
     * @return int
     */
    public int index() {
        return rownum.get() - 1;
    }

    @Override
    public String toString() {
        return String.valueOf(rownum);
    }

    public static void main(String[] args) {
        final Rownum rownum = Rownum.of(0);
        for (int i = 0; i < 10; i++) {
            System.out.println(Arrays.asList(rownum.next().get(), rownum.index()));
        }
        final Rownum rm = Rownum.of(1);
        System.out.println(rm.poll());
        System.out.println(rm.poll());
    }
}
