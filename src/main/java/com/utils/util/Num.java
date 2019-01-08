package com.utils.util;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.utils.util.Num.Pattern.*;

/**
 * 数字转换类；使用此类初始化时，会尽可能的处理 null 值，避免因为 null 值抛出异常；但部分无法适配的操作依然会抛异常
 * 请谨慎使用此类，因为可以用 null 值初始化，在计算时因为数据转换产生的 null 值会产生警告，造成计算结果有差异
 *
 * @author 谢长春 on 2017/10/26 .
 */
@Slf4j
public class Num {
    /**
     * 枚举：定义数字格式
     */
    public enum Pattern {
        // 不带千位符，保留0位小数
        LONG("##0"),
        // 不带千位符，保留1位小数
        FLOAT("##0.0"),
        // 不带千位符，保留2位小数
        DOUBLE("##0.00"),
        // 不带千位符，保留 fixed 位小数；调用value(fixed)方法指定fixed
        AUTO("##0"),

        // 带千位符，保留0位小数
        SLONG("#,##0"),
        // 带千位符，保留1位小数
        SFLOAT("#,##0.0"),
        // 带千位符，保留2位小数
        SDOUBLE("#,##0.00"),
        // 带千位符，保留 fixed 位小数；调用value(fixed)方法指定fixed
        SAUTO("#,##0"),
        ;
        /**
         * 格式
         */
        final String pattern;
        final Function<Number, String> format;
        final BiFunction<Number, Integer, String> fixed;

        Pattern(final String pattern) {
            this.pattern = pattern;
            this.format = v -> new DecimalFormat(pattern).format(v);
            switch (this.name()) {
                case "AUTO":
                case "SAUTO":
                    this.fixed = (v, fixed) ->
                            new DecimalFormat((fixed <= 0) ? this.pattern : this.pattern.concat(".").concat(String.format("%0{fixed}d".replace("{fixed}", Objects.toString(fixed)), 0))).format(v);
                    break;
                default:
                    this.fixed = null;
            }
        }

        /**
         * 格式化数字
         *
         * @param v {@link Number} 被格式化值
         * @return {@link String}
         */
        public String format(final Number v) {
            return format.apply(v);
        }

        /**
         * 格式化数字
         *
         * @param v     {@link Number} 被格式化值
         * @param fixed {@link int} 保留小数位
         * @return {@link String}
         */
        public String format(final Number v, final int fixed) {
            return this.fixed.apply(v, fixed);
        }
    }

    public interface IRange<T extends Number> {
        T getMin();

        T getMax();

        /**
         * 检查 value 是否在 min,max 区间内；包含 min,max
         *
         * @param value {@link Double} 被检查的值
         * @return boolean true：是，false：否
         */
        default boolean in(final Double value) {
            return Objects.nonNull(value) && getMin().doubleValue() <= value && value <= getMax().doubleValue();
        }

        /**
         * 检查 value 是否在 min,max 区间内；包含 min,max
         *
         * @param value {@link Float} 被检查的值
         * @return boolean true：是，false：否
         */
        default boolean in(final Float value) {
            return Objects.nonNull(value) && getMin().floatValue() <= value && value <= getMax().floatValue();
        }

        /**
         * 检查 value 是否在 min,max 区间内；包含 min,max
         *
         * @param value {@link Long} 被检查的值
         * @return boolean true：是，false：否
         */
        default boolean in(final Long value) {
            return Objects.nonNull(value) && getMin().longValue() <= value && value <= getMax().longValue();
        }

        /**
         * 检查 value 是否在 min,max 区间内；包含 min,max
         *
         * @param value {@link Integer} 被检查的值
         * @return boolean true：是，false：否
         */
        default boolean in(final Integer value) {
            return Objects.nonNull(value) && getMin().intValue() <= value && value <= getMax().intValue();
        }

        /**
         * 检查 value 是否在 min,max 区间内；包含 min,max
         *
         * @param value {@link Short} 被检查的值
         * @return boolean true：是，false：否
         */
        default boolean in(final Short value) {
            return Objects.nonNull(value) && getMin().shortValue() <= value && value <= getMax().shortValue();
        }

        /**
         * 检查 value 是否在 min,max 区间内；不包含 min,max
         *
         * @param value {@link Double} 被检查的值
         * @return boolean true：是，false：否
         */
        default boolean round(final Double value) {
            return Objects.nonNull(value) && getMin().doubleValue() < value && value < getMax().doubleValue();
        }

        /**
         * 检查 value 是否在 min,max 区间内；不包含 min,max
         *
         * @param value {@link Float} 被检查的值
         * @return boolean true：是，false：否
         */
        default boolean round(final Float value) {
            return Objects.nonNull(value) && getMin().floatValue() < value && value < getMax().floatValue();
        }

        /**
         * 检查 value 是否在 min,max 区间内；不包含 min,max
         *
         * @param value {@link Long} 被检查的值
         * @return boolean true：是，false：否
         */
        default boolean round(final Long value) {
            return Objects.nonNull(value) && getMin().longValue() < value && value < getMax().longValue();
        }

        /**
         * 检查 value 是否在 min,max 区间内；不包含 min,max
         *
         * @param value {@link Integer} 被检查的值
         * @return boolean true：是，false：否
         */
        default boolean round(final Integer value) {
            return Objects.nonNull(value) && getMin().intValue() < value && value < getMax().intValue();
        }

        /**
         * 检查 value 是否在 min,max 区间内；不包含 min,max
         *
         * @param value {@link Short} 被检查的值
         * @return boolean true：是，false：否
         */
        default boolean round(final Short value) {
            return Objects.nonNull(value) && getMin().shortValue() < value && value < getMax().shortValue();
        }

    }

    /**
     * 构造数字处理对象， 可以为null；但是有值，数据类型转换失败，则抛出异常
     *
     * @param value Object
     * @return {@link Num}
     */
    public static Num of(Object value) {
        if (Objects.isNull(value) || Objects.equals(value, "")) {
            return new Num();
        }
        return of(Objects.toString(value));
    }

    /**
     * 构造数字处理对象， 可以为null；但是有值，数据类型转换失败，则抛出异常
     *
     * @param value Object
     * @return {@link Num}
     */
    public static Num of(Object value, Number defaultValue) {
        if (Objects.isNull(value)) {
            return new Num(defaultValue);
        }
        if (value instanceof Integer) {
            of((Integer) value);
        } else if (value instanceof Long) {
            of((Long) value);
        } else if (value instanceof Double) {
            of((Double) value);
        } else if (value instanceof Short) {
            of((Short) value);
        } else if (value instanceof Float) {
            of((Float) value);
        } else if (value instanceof BigInteger) {
            of((BigInteger) value);
        } else if (value instanceof BigDecimal) {
            of((BigDecimal) value);
        }
        return of(Objects.toString(value), defaultValue);
    }

    /**
     * 构造数字处理对象， 不能为null，转换失败则抛出异常
     *
     * @param value Number
     * @return {@link Num}
     */
    public static Num of(Number value) {
        return new Num(value);
    }

    /**
     * 构造数字处理对象， 不能为null，转换失败则抛出异常
     *
     * @param value String
     * @return {@link Num}
     */
    public static Num of(String value) {
        return new Num(Double.valueOf(value.trim().replace(",", "")));
    }

    /**
     * 构造指定默认值的数字处理对象，且不抛异常；当数据转换失败时，有默认值则设置默认值，未设置则默认值为null；
     *
     * @param value        String 数字字符串
     * @param defaultValue 为空时的默认值
     * @return {@link Num}
     */
    public static Num of(String value, Number defaultValue) {
        try {
            return Objects.isNull(value) ? new Num() : new Num(Double.valueOf(value.trim().replace(",", "")));
        } catch (NumberFormatException e) {
            log.info("value=" + value);
            return Objects.isNull(defaultValue) ? new Num() : new Num(defaultValue.doubleValue());
        }
    }

    /**
     * 构造允许空值的数字处理对象，且不抛异常；当数据转换失败时，默认值为null
     *
     * @param value String
     * @return {@link Num}
     */
    public static Num ofNull(String value) {
        return of(value, null);
    }

    /**
     * 构造允许空值的数字处理对象，且不抛异常；当数据转换失败时，默认值为 0
     *
     * @param value String
     * @return {@link Num}
     */
    public static Num ofZore(String value) {
        return of(value, 0);
    }

    /**
     * 构造允许空值的数字处理对象，且不抛异常；默认值为 0
     *
     * @param value Number
     * @return {@link Num}
     */
    public static Num ofZore(Number value) {
        if (Objects.isNull(value)) {
            return new Num(0);
        }
        return new Num(value);
    }

    private Num() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) { // 收集最近代码位置
            try {
                sb.append("\n").append(Thread.currentThread().getStackTrace()[2 + i].toString());
            } catch (ArrayIndexOutOfBoundsException e) {
                i = 3;
            }
        }
        log.warn("警告：使用null值初始化Num数字操作对象，计算时结果可能与实际结果有差异；建议使用: Num.of(\"1000\", 0) ,尽可能控制所有未知的情况，避免计算出错" + sb.toString());
    }

    private Num(Number value) {
        set(value);
    }

    private Double value;
    /**
     * 调用format时，默认的格式
     */
    private Pattern defaultPattern = DOUBLE;

    public boolean isNull() {
        return Objects.isNull(value);
    }

    public boolean isNotNull() {
        return !isNull();
    }

    public Num set(String value) {
        return new Num(Double.valueOf(value.trim().replace(",", "")));
    }

    public Num set(String value, Number defaultValue) {
        try {
            return new Num(Double.valueOf(value.trim().replace(",", "")));
        } catch (NumberFormatException e) {
            return new Num(defaultValue.doubleValue());
        }
    }

    public Num set(Number value) {
        if (value instanceof Integer || value instanceof Long || value instanceof Short) {
            defaultPattern = LONG; // 设置默认的格式
        }
        this.value = value.doubleValue();
        return this;
    }

    /**
     * 数值增加，求和时使用
     *
     * @param v Num
     * @return {@link Num}
     */
    public Num add(Num v) {
        if (Objects.nonNull(v)) {
            value = this.doubleValue() + v.doubleValue();
        }
        return this;
    }

    /**
     * 数值增加，求和时使用
     *
     * @param v Number
     * @return {@link Num}
     */
    public Num add(Number v) {
        if (Objects.nonNull(v)) {
            value = this.doubleValue() + v.doubleValue();
        }
        return this;
    }

    /**
     * 数值增加，求和时使用
     *
     * @param values Number
     * @return {@link Num}
     */
    public Num add(Number... values) {
        if (Util.isNotEmpty(values)) {
            value = this.doubleValue() + Arrays.stream(values).filter(Objects::nonNull).mapToDouble(Number::doubleValue).sum();
        }
        return this;
    }

    /**
     * 转换为 Double 类型，返回值可能为null,操作异常则返回null
     *
     * @return Double
     */
    public Double toDouble() {
        return value;
    }

    /**
     * 转换为 double 类型，返回值不能为null，操作异常则返回0
     *
     * @return double
     */
    public double doubleValue() {
        return (Objects.isNull(value)) ? 0D : value;
    }

    /**
     * 转换为 Long 类型，返回值可能为null,操作异常则返回null
     *
     * @return Long
     */
    public Long toLong() {
        return (Objects.isNull(value)) ? null : value.longValue();
    }

    /**
     * 转换为 long 类型，返回值不能为null，操作异常则返回0
     *
     * @return long
     */
    public long longValue() {
        return (Objects.isNull(value)) ? 0L : value.longValue();
    }

    /**
     * 转换为 Integer 类型，返回值可能为null,操作异常则返回null
     *
     * @return Integer
     */
    public Integer toInteger() {
        return (Objects.isNull(value)) ? null : value.intValue();
    }

    /**
     * 转换为 int 类型，返回值不能为null，操作异常则返回0
     *
     * @return int
     */
    public int intValue() {
        return (Objects.isNull(value)) ? 0 : value.intValue();
    }

    /**
     * 转换为 Float 类型，返回值可能为null,操作异常则返回null
     *
     * @return Float
     */
    public Float toFloat() {
        return (Objects.isNull(value)) ? null : value.floatValue();
    }

    /**
     * 转换为 float 类型，返回值不能为null，操作异常则返回0
     *
     * @return float
     */
    public float floatValue() {
        return (Objects.isNull(value)) ? 0F : value.floatValue();
    }

    /**
     * 转换为 Short 类型，返回值可能为null,操作异常则返回null
     *
     * @return Short
     */
    public Short toShort() {
        return (Objects.isNull(value)) ? null : value.shortValue();
    }

    /**
     * 转换为 short 类型，返回值不能为null，操作异常则返回0
     *
     * @return short
     */
    public short shortValue() {
        return (Objects.isNull(value)) ? 0 : value.shortValue();
    }

    /**
     * 转换为 BigDecimal 类型，返回值可能为null,操作异常则返回null
     *
     * @return BigDecimal
     */
    public BigDecimal toBigDecimal() {
        return (Objects.isNull(value)) ? null : new BigDecimal(value);
    }

    /**
     * 转换为 BigDecimal 类型，返回值不能为null，操作异常则返回0
     *
     * @return BigDecimal
     */
    public BigDecimal bigDecimalValue() {
        return (Objects.isNull(value)) ? BigDecimal.ZERO : new BigDecimal(value);
    }

    /**
     * 将 long 数字转换为日期操作对象
     *
     * @return {@link Dates}
     */
    public Dates toDate() {
        return longValue() > 0 ? Dates.of(longValue()) : null;
    }

    /**
     * 将 value 按格式取对应的值；不建议使用此方法，因为得到的Number对象还得继续取值；但在某些情况，数字类型不明确时可以使用
     *
     * @param pattern Pattern
     * @return Number
     */
    public Number getNumber(Pattern pattern) {
        switch (pattern) {
            case LONG:
            case SLONG:
                return longValue();
            case FLOAT:
            case SFLOAT:
                return floatValue();
            case DOUBLE:
            case SDOUBLE:
                return doubleValue();
            default:
                break;
        }
        return value;
    }

    /**
     * 格式化数字，默认格式：0000.00保留两位小数,不含千位符
     *
     * @return String 格式化后的字符串
     */
    public String format() {
        return format(defaultPattern);
    }

    /**
     * 格式化数字，默认格式：0000.00保留两位小数,不含千位符
     *
     * @param pattern String 格式
     * @return String 格式化后的字符串
     */
    public String format(String pattern) {
        if (Objects.isNull(value)) {
            return null;
        }
        if (Objects.isNull(pattern)) {
            return Pattern.DOUBLE.format(value);
        }
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * 格式化数字，默认格式：0000.00保留两位小数,不含千位符
     *
     * @param pattern String 格式
     * @return String 格式化后的字符串
     */
    public String format(Pattern pattern) {
        if (Objects.isNull(value)) {
            return null;
        }
        if (Objects.isNull(pattern)) {
            return Pattern.DOUBLE.format(value);
        }
        return pattern.format(value);
    }

    /**
     * 格式化金额，默认格式：#,##0.00保留两位小数,含千位符
     *
     * @return String 格式化后的字符串
     */
    public String formatAmount() {
        if (Objects.isNull(value)) {
            return null;
        }
        return SDOUBLE.format(value);
    }

    @Override
    public String toString() {
        return format();
    }

    public static void main(String[] args) {
        log.info("{}", RangeInt.of(0, 10).toString());
        log.info("{}", Num.ofNull(" ").toInteger());
        log.info("{}", Num.ofNull(" ").intValue());
        log.info("{}", Num.ofNull(null).toDouble());
        log.info("{}", Num.ofNull(null).doubleValue());
        log.info("{}", Num.ofNull(" ").toBigDecimal());
        log.info("{}", Num.ofNull(" ").bigDecimalValue());
        log.info("{}", Num.of(null, null));
        log.info("{}", Num.of("", null));
        log.info("---------");
        log.info("{}", Num.of(1000.01).toInteger());
        log.info("{}", Num.of(1000.01).intValue());
        log.info("{}", Num.of(1000.01).toDouble());
        log.info("{}", Num.of(1000.01).doubleValue());
        log.info("{}", Num.of(1000.01).toBigDecimal());
        log.info("{}", Num.of(1000.01).bigDecimalValue());

        log.info(">>>>>>>");
        log.info(Num.of(Integer.MAX_VALUE).formatAmount());
        log.info(Num.of(Integer.MIN_VALUE).formatAmount());
        log.info(Num.of(Short.MAX_VALUE).formatAmount());
        log.info(Num.of(Short.MIN_VALUE).formatAmount());
        log.info(Num.of(Float.MAX_VALUE).formatAmount());
        log.info(Num.of(Float.MIN_VALUE).formatAmount());
        log.info(Num.of(Double.MAX_VALUE).formatAmount());
        log.info(Num.of(Double.MIN_VALUE).formatAmount());
        log.info(Num.of(BigDecimal.ZERO).formatAmount());
        log.info(Num.of(BigDecimal.ONE).formatAmount());
        log.info(Num.of("1000", 0).formatAmount());
        log.info(Num.of("1000.01").formatAmount());
        log.info("{}", Num.of("100,100.00").add(1.01).toInteger());
        log.info("{}", Num.of("100,100.00").add(1.01).doubleValue());
        log.info("{}", Num.of("100,100.00").add(1.01, 1.01, null, 1.01).doubleValue());
        log.info(Num.of("100,100.00").add(1.01, 1.01, null, 1.01).toString());
        log.info(Num.of("100,100.00").add(1.01, 1.01, null, 1.01).format(Pattern.SDOUBLE));

        log.info(">>>>>>>");
        log.info(Num.of(1000000).format(LONG));
        log.info(Num.of("-1000000").format(LONG));
        log.info(Num.of(1000000).format(FLOAT));
        log.info(Num.of("-1000000").format(FLOAT));
        log.info(Num.of(1000000).format(DOUBLE));
        log.info(Num.of("-1000000").format(DOUBLE));

        log.info(Num.of(1000000).format(SLONG));
        log.info(Num.of("-1000000").format(SLONG));
        log.info(Num.of(1000000).format(SFLOAT));
        log.info(Num.of("-1000000").format(SFLOAT));
        log.info(Num.of(1000000).format(SDOUBLE));
        log.info(Num.of("-1000000").format(SDOUBLE));
        log.info(AUTO.format(1000000, 4));
        log.info(AUTO.format(-1000000, 4));
        log.info(SAUTO.format(1000000, 4));
        log.info(SAUTO.format(-1000000, 4));

        log.info(">>>>>>>");
        log.info("0 in 1-10 : {}", RangeInt.of(1, 10).in(0));
        log.info("1 in 1-10 : {}", RangeInt.of(1, 10).in(1));
        log.info("2 in 1-10 : {}", RangeInt.of(1, 10).in(2));
        log.info("9 in 1-10 : {}", RangeInt.of(1, 10).in(9));
        log.info("10 in 1-10 : {}", RangeInt.of(1, 10).in(10));
        log.info("11 in 1-10 : {}", RangeInt.of(1, 10).in(11));
        log.info("0 round 1-10 : {}", RangeInt.of(1, 10).round(0));
        log.info("1 round 1-10 : {}", RangeInt.of(1, 10).round(1));
        log.info("2 round 1-10 : {}", RangeInt.of(1, 10).round(2));
        log.info("9 round 1-10 : {}", RangeInt.of(1, 10).round(9));
        log.info("10 round 1-10 : {}", RangeInt.of(1, 10).round(10));
        log.info("11 round 1-10 : {}", RangeInt.of(1, 10).round(11));
        log.info("range int array: {}", RangeInt.of(new Integer[]{9, 8, 1, 5, 4}));
        log.info("range long array: {}", RangeLong.of(new Long[]{9L, 8L, 1L, 5L, 4L}));
        log.info("range int list: {}", RangeInt.of(JSON.parseArray("[9, 8, 1, 5, 4]", Integer.class)));
        log.info("range long list: {}", RangeLong.of(JSON.parseArray("[9, 8, 1, 5, 4]", Long.class)));
    }
}
