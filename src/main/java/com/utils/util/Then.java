package com.utils.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 构建链式调用，只能操作对象，不支持原始数据类型
 *
 * @author 谢长春 2019/1/5 .
 */
public class Then<T> {
    /**
     * 构造链式调用对象
     *
     * @param obj T
     * @return {@link Then}
     */
    public static <T> Then<T> of(final T obj) {
        return new Then<>(Objects.requireNonNull(obj, "参数【obj】不能为null"));
    }

    private Then() {
        this.obj = null;
    }

    public Then(final T obj) {
        this.obj = obj;
    }

    private final T obj;

    /**
     * 获取链式操作的值
     *
     * @return T
     */
    public T get() {
        return obj;
    }

    /**
     * 执行 consumer
     *
     * @param consumer {@link Consumer}
     * @return {@link Then}
     */
    public Then<T> then(final Consumer<T> consumer) {
        Objects.requireNonNull(consumer, "参数【consumer】不能为null").accept(obj);
        return this;
    }

    /**
     * Objects.nonNull(value) 时才执行 consumer
     *
     * @param value    {@link Object}
     * @param consumer {@link Consumer}
     * @return {@link Then}
     */
    public Then<T> then(final Object value, final Consumer<T> consumer) {
        if (Objects.nonNull(value))
            Objects.requireNonNull(consumer, "参数【consumer】不能为null").accept(obj);
        return this;
    }

    /**
     * hasTrue == true 时才执行 consumer
     *
     * @param hasTrue  boolean
     * @param consumer {@link Consumer}
     * @return {@link Then}
     */
    public Then<T> then(final boolean hasTrue, final Consumer<T> consumer) {
        if (hasTrue)
            Objects.requireNonNull(consumer, "参数【consumer】不能为null").accept(obj);
        return this;
    }

    /**
     * 执行 function
     *
     * @param function {@link Function}
     * @return {@link Then}
     */
    public Then<T> map(final Function<T, T> function) {
        return Then.of(Objects.requireNonNull(function, "参数【function】不能为null").apply(obj));
    }

    public static void main(String[] args) {
        System.out.println(
                Then.of(RangeInt.builder().min(0).max(10).build())
                        .then(range -> System.out.println("10:" + range.toString()))
                        .then(range -> range.setMax(100))
                        .then(range -> System.out.println("100:" + range.toString()))
                        .then(null, range -> range.setMax(1000))
                        .then(range -> System.out.println("100:" + range.toString()))
                        .then("", range -> range.setMax(1000))
                        .then(range -> System.out.println("1000:" + range.toString()))
                        .then(false, range -> range.setMax(10000))
                        .then(range -> System.out.println("1000:" + range.toString()))
                        .then(true, range -> range.setMax(10000))
                        .then(range -> System.out.println("10000:" + range.toString()))
                        .then(range -> range.setMin(1))
                        .then(range -> System.out.println(range.toString()))
                        .map(range -> RangeInt.builder().min(range.getMin()).max(1).build())
                        .get()
                        .toString()
        );
    }

}
