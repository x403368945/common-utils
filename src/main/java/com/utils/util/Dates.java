package com.utils.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.utils.excel.enums.Week;
import lombok.*;
import lombok.Builder;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Calendar.*;

/**
 * 日期处理类
 *
 * @author Jason Xie on 2017/10/28.
 */
@Slf4j
public class Dates {
    private static final TimeZone timeZone = TimeZone.getTimeZone("GMT+8");

    /**
     * 枚举：定义日期格式
     */
    public enum Pattern {
        yyyy("yyyy"),
        MM("MM"),
        dd("dd"),
        HH_mm_ss("HH:mm:ss"),
        HH_mm("HH:mm"),
        HH("HH"),
        mm("mm"),
        ss("ss"),
        yyyy_MM_dd("yyyy-MM-dd"),
        yyyy_MM("yyyy-MM"),
        yyyyMMdd("yyyyMMdd"),
        yyyyMM("yyyyMM"),
        yyyy_MM_dd_HH_mm_ss("yyyy-MM-dd HH:mm:ss"),
        yyyy_MM_dd_HHmmss("yyyy-MM-dd HHmmss"),
        yyyyMMddHHmmss("yyyyMMddHHmmss"),
        yyyyMMddHHmmssSSS("yyyyMMddHHmmssSSS"),
        yy_MM_dd("yy-MM-dd"),
        HHmmssSSS("HHmmssSSS"),
        HHmmss("HHmmss"),

        zh_yyyy_MM_dd_HH_mm_ss("yyyy年MM月dd日 HH时mm分"),
        zh_yyyy_MM_dd("yyyy年MM月dd日"),
        zh_yyyy_MM("yyyy年MM月"),;
        /**
         * 枚举属性说明
         */
        private final String comment;

        public String value() {
            return this.comment;
        }

        Pattern(String comment) {
            this.comment = comment;
        }
    }

    /**
     * @deprecated 未完善, 暂时不可用
     */
    @AllArgsConstructor
    @Builder
    @Accessors(fluent = true, chain = true)
    @Deprecated
    public static class DateObject {
        private static DateObject parse(String jsonText) { // "{'y':0,'m':0,'d':0,'h':0,'mm':0,'s':0,'ms':0}"
            JSONObject obj = JSON.parseObject(jsonText.replace("'", "\""));
            int y = obj.getIntValue("y");
            int m = obj.getIntValue("m");
            int d = obj.getIntValue("d");
            int h = obj.getIntValue("h");
            int mm = obj.getIntValue("mm");
            int s = obj.getIntValue("s");
            int ms = obj.getIntValue("ms");
            DateObject dateObject = DateObject.builder().y(y).m(m).d(d).h(h).mm(mm).s(s).ms(ms).build();
            if (m > 12 && d <= 12) { // m 和 d 交换位置
                dateObject.m = d;
                dateObject.d = m;
            }
            return dateObject;
        }

        /**
         * 识别日期格式
         *
         * @param value String
         * @return {@link DateObject}
         */
        public static DateObject of(String value) {
            if (Objects.isNull(value) || "".equals(value.trim())) {
                return DateObject.builder().build();
            }
            try {
                value = value.trim();
//                "{'y':0,'m':0,'d':0,'h':0,'mm':0,'s':0,'ms':0}"
                if (value.replace(" ", "").matches("^\\d+$")) { // 纯数字
                    switch (value.length()) {
                        case 6: // 6 位数字，时分秒 > HHmmss
                            return parse(value.replaceAll("^(\\d{2})(\\d{2})(\\d{2})$", "{'y':0,'m':0,'d':0,'h':$1,'mm':$2,'s':$3,'ms':0}"));
                        case 8: // 8 位数字，年月日 > yyyyMMdd
                            return parse(value.replaceAll("^(\\d{4})(\\d{2})(\\d{2})$", "{'y':$1,'m':$2,'d':$3,'h':0,'mm':0,'s':0,'ms':0}"));
                        case 9: // 9 位数字，时分秒毫秒 > HHmmssSSS
                            return parse(value.replaceAll("^(\\d{2})(\\d{2})(\\d{2})(\\d{3})$", "{'y':0,'m':0,'d':0,'h':$1,'mm':$2,'s':$3,'ms':$4}"));
                        case 14: // 14 位数字，年月日时分秒 > yyyyMMddHHmmss
                            return parse(value.replaceAll("^(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})$", "{'y':$1,'m':$2,'d':$3,'h':$4,'mm':$5,'s':$6,'ms':0}"));
                        case 17: // 17 位数字，年月日时分秒毫秒 > yyyyMMddHHmmssSSS
                            return parse(value.replaceAll("^(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{3})$", "{'y':$1,'m':$2,'d':$3,'h':$4,'mm':$5,'s':$6,'ms':$7}"));
                        default:
                            throw new IllegalArgumentException("未识别的日期格式");
                    }
                }
                if (value.matches("^\\d{4}.\\d{1,2}.\\d{1,2} \\d{1,2}.\\d{1,2}.\\d{1,2}$")) { // yyyyMMddHHmmss
                    return parse(value.replace("^(\\d{4}).(\\d{1,2}).(\\d{1,2}) (\\d{1,2}).(\\d{1,2}).(\\d{1,2})$", "{'y':$1,'m':$2,'d':$3,'h':$4,'mm':$5,'s':$6,'ms':0}"));
                }
                if (value.matches("^\\d{4}.\\d{1,2}.\\d{1,2}[T ]\\d{1,2}.\\d{1,2}.\\d{1,2}.\\d{1,3}(Z)?$")) { // yyyyMMddHHmmssSSS
                    return parse(value.replace("^(\\d{4}).(\\d{1,2}).(\\d{1,2})[T ](\\d{1,2}).(\\d{1,2}).(\\d{1,2}).(\\d{1,3})(Z)?$", "{'y':$1,'m':$2,'d':$3,'h':$4,'mm':$5,'s':$6,'ms':$7}"));
                }
                if (value.matches("^\\d{4}.\\d{1,2}.\\d{1,2}$")) { // yyyyMMdd
                    return parse(value.replaceAll("^(\\d{4})(\\d{1,2})(\\d{1,2})$", "{'y':$1,'m':$2,'d':$3,'h':0,'mm':0,'s':0,'ms':0}"));
                }
                if (value.matches("^\\d{1,2}.\\d{1,2}.\\d{1,2}$")) { // hhmmss
                    return parse(value.replaceAll("^(\\d{2})(\\d{1,2})(\\d{1,2})$", "{'y':0,'m':0,'d':0,'h':$1,'mm':$2,'s':$3,'ms':0}"));
                }
                throw new IllegalArgumentException("未识别的日期格式");
            } catch (Exception e) {
                throw new IllegalArgumentException("日期转换失败，value:".concat(value));
            }
        }

        private int y;
        private int m;
        private int d;
        private int h;
        private int mm;
        private int s;
        private int ms;

        public Dates toDates() {
            return new Dates().year(y).month(m).day(d).h(h).m(mm).s(s).ms(ms);
        }

        public Date toDate() {
            return toDates().date();
        }

        public Date toTimestamp() {
            return toDates().timestamp();
        }
    }

    /**
     * 定义日期区间
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class Range {
        /**
         * 开始
         */
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Timestamp begin;
        /**
         * 结束
         */
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Timestamp end;

        /**
         * 遍历选定区间：按天
         *
         * @param action {@link BiConsumer<Timestamp:start, Timestamp:end> } <br>start=2018-01-01 00:00:00.000 <br>end=2018-01-01 23:59:59.999
         */
        public void forEach(BiConsumer<Timestamp, Timestamp> action) {
            Objects.requireNonNull(action, "参数【action】是必须的");
            final Dates beginDate = Dates.of(begin);
            final Dates endDate = Dates.of(end).endTimeOfDay();
            do {
                action.accept(beginDate.beginTimeOfDay().timestamp(), beginDate.endTimeOfDay().timestamp());
                beginDate.addDay(1);
            } while (beginDate.le(endDate));
        }

        /**
         * 遍历选定区间：按月
         *
         * @param action {@link BiConsumer<Timestamp:start, Timestamp:end> } <br>start=2018-01-01 00:00:00.000 <br>end=2018-01-31 23:59:59.999
         */
        public void forEachMonth(BiConsumer<Timestamp, Timestamp> action) {
            Objects.requireNonNull(action, "参数【action】是必须的");
            final Dates beginDate = Dates.of(begin);
            final Dates endDate = Dates.of(end).lastDayOfMonth();
            do {
                action.accept(beginDate.firstDayOfMonth().timestamp(), beginDate.lastDayOfMonth().timestamp());
                beginDate.addMonth(1);
            } while (beginDate.le(endDate));
        }

        /**
         * 保留年月日，将开始时间设置为 00:00:00
         * 保留年月日，将结束时间设置为 23:59:59
         *
         * @return {@link Range}
         */
        public Range rebuild() {
            if (Objects.nonNull(begin)) begin = Dates.of(begin).beginTimeOfDay().timestamp();
            if (Objects.nonNull(end)) end = Dates.of(end).endTimeOfDay().timestamp();
            return this;
        }

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }
    }

    /**
     * 以当前时间 构造时间处理对象
     *
     * @return {@link Dates}
     */
    public static Dates now() {
        return new Dates();
    }

    /**
     * 构造时间处理对象：指定时间
     *
     * @param value Timestamp
     * @return {@link Dates}
     */
    public static Dates of(final Timestamp value) {
        Objects.requireNonNull(value, "参数【value】是必须的");
        return new Dates(value.getTime());
    }

    /**
     * 构造时间处理对象：指定时间
     *
     * @param value Date
     * @return {@link Dates}
     */
    public static Dates of(final Date value) {
        Objects.requireNonNull(value, "参数【value】是必须的");
        return new Dates(value.getTime());
    }

    /**
     * 构造时间处理对象：指定时间
     *
     * @param value Timestamp
     * @return {@link Dates}
     */
    public static Dates of(final long value) {
        return new Dates(value);
    }

    /**
     * 构造时间处理对象：指定时间
     *
     * @param value   String 日期字符串
     * @param pattern {@link Pattern} 日期格式
     * @return {@link Dates}
     */
    public static Dates of(final String value, final Pattern pattern) {
        Objects.requireNonNull(value, "参数【value】是必须的");
        Objects.requireNonNull(pattern, "参数【pattern】是必须的");
        try {
            return new Dates(new SimpleDateFormat(pattern.value()).parse(value).getTime());
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("日期转换失败，value:%s > pattern:%s", value, pattern));
        }
    }

    private Dates() {
    }

    private Dates(long value) {
        this.calendar.setTimeInMillis(value);
    }

    private final Calendar calendar = Calendar.getInstance(timeZone);

    /**
     * 转换为long
     *
     * @return long
     */
    public long get() {
        return calendar.getTimeInMillis();
    }

    /**
     * 转换为 Timestamp
     *
     * @return Timestamp
     */
    public Timestamp timestamp() {
        return new Timestamp(calendar.getTimeInMillis());
    }

    /**
     * 转换为 Date
     *
     * @return {@link Date}
     */
    public Date date() {
        return calendar.getTime();
    }

    /**
     * 格式化为字符串, 必须指定格式
     *
     * @param pattern Pattern
     * @return String
     */
    public String format(final Pattern pattern) {
        Objects.requireNonNull(pattern, "参数【pattern】是必须的");
        return format(pattern.value());
    }

    /**
     * 格式化为字符串, 必须指定格式
     *
     * @param pattern Pattern
     * @return String
     */
    public String format(final String pattern) {
        Objects.requireNonNull(pattern, "参数【pattern】是必须的");
        return new SimpleDateFormat(pattern).format(calendar.getTime());
    }

    /**
     * 格式化为字符串, 示例：yyyy-MM-dd
     *
     * @return String
     */
    public String formatDate() {
        return format(Pattern.yyyy_MM_dd);
    }

    /**
     * 格式化为字符串, 示例：HH:mm:ss
     *
     * @return String
     */
    public String formatTime() {
        return format(Pattern.HH_mm_ss);
    }

    /**
     * 格式化为字符串, 示例：yyyy-MM-dd HH:mm:ss
     *
     * @return String
     */
    public String formatDateTime() {
        return format(Pattern.yyyy_MM_dd_HH_mm_ss);
    }

    /**
     * 获取：年
     *
     * @return int
     */
    public int year() {
        return calendar.get(YEAR);
    }

    /**
     * 获取：月
     *
     * @return int
     */
    public int month() {
        return calendar.get(MONTH) + 1;
    }

    /**
     * 获取：日
     *
     * @return int
     */
    public int day() {
        return calendar.get(DAY_OF_MONTH);
    }

    /**
     * 获取：星期
     *
     * @return {@link Week}
     */
    public Week week() {
        return Week.values()[calendar.get(DAY_OF_WEEK) - 1];
    }

    /**
     * 获取：时
     *
     * @return int
     */
    public int h() {
        return calendar.get(HOUR);
    }

    /**
     * 获取：分
     *
     * @return int
     */
    public int m() {
        return calendar.get(MINUTE);
    }

    /**
     * 获取：秒
     *
     * @return int
     */
    public int s() {
        return calendar.get(SECOND);
    }

    /**
     * 获取：毫秒
     *
     * @return int
     */
    public int ms() {
        return calendar.get(MILLISECOND);
    }

    /**
     * 指定：年
     *
     * @param value int
     * @return {@link Dates}
     */
    public Dates year(int value) {
        calendar.set(YEAR, value);
        return this;
    }

    /**
     * 指定：月
     *
     * @param value int
     * @return {@link Dates}
     */
    public Dates month(int value) {
        calendar.set(MONTH, Math.min(12, value) - 1);
        return this;
    }

    /**
     * 指定：日
     *
     * @param value int
     * @return {@link Dates}
     */
    public Dates day(int value) {
        calendar.set(DAY_OF_MONTH, Math.min(calendar.getActualMaximum(DAY_OF_MONTH), value));
        return this;
    }

    /**
     * 指定：时
     *
     * @param value int
     * @return {@link Dates}
     */
    public Dates h(int value) {
        calendar.set(HOUR_OF_DAY, Math.min(23, value));
        return this;
    }

    /**
     * 指定：分
     *
     * @param value int
     * @return {@link Dates}
     */
    public Dates m(int value) {
        calendar.set(MINUTE, Math.min(59, value));
        return this;
    }

    /**
     * 指定：秒
     *
     * @param value int
     * @return {@link Dates}
     */
    public Dates s(int value) {
        calendar.set(SECOND, Math.min(59, value));
        return this;
    }

    /**
     * 指定：秒
     *
     * @param value int
     * @return {@link Dates}
     */
    public Dates ms(int value) {
        calendar.set(MILLISECOND, Math.min(999, value));
        return this;
    }

    /**
     * 年【增加|减少】
     *
     * @param value int 正数为增加，负数表示减少
     * @return {@link Dates}
     */
    public Dates addYear(int value) {
        calendar.add(YEAR, value);
        return this;
    }

    /**
     * 月【增加|减少】
     *
     * @param value int 正数为增加，负数表示减少
     * @return {@link Dates}
     */
    public Dates addMonth(int value) {
        calendar.add(MONTH, value);
        return this;
    }

    /**
     * 日【增加|减少】
     *
     * @param value int 正数为增加，负数表示减少
     * @return {@link Dates}
     */
    public Dates addDay(int value) {
        calendar.add(DAY_OF_MONTH, value);
        return this;
    }

    /**
     * 星期【增加|减少】
     *
     * @param value int 正数为增加，负数表示减少
     * @return {@link Dates}
     */
    public Dates addWeek(int value) {
        calendar.add(WEEK_OF_YEAR, value);
        return this;
    }

    /**
     * 时【增加|减少】
     *
     * @param value int 正数为增加，负数表示减少
     * @return {@link Dates}
     */
    public Dates addHour(int value) {
        calendar.add(HOUR, value);
        return this;
    }

    /**
     * 分【增加|减少】
     *
     * @param value int 正数为增加，负数表示减少
     * @return {@link Dates}
     */
    public Dates addMinute(int value) {
        calendar.add(MINUTE, value);
        return this;
    }

    /**
     * 秒【增加|减少】
     *
     * @param value int 正数为增加，负数表示减少
     * @return {@link Dates}
     */
    public Dates addSecond(int value) {
        calendar.add(SECOND, value);
        return this;
    }

    /**
     * 计算并设置为上周一的日期
     *
     * @return {@link Dates}
     */
    public Dates prevMonday() {
        // addWeek(-1) ；即上周
        addWeek(-1);
        // 设置为周一
        calendar.set(DAY_OF_WEEK, Week.Mon.ordinal() + 1);
        return this;
    }

    /**
     * 计算并设置为下周一的日期
     *
     * @return {@link Dates}
     */
    public Dates nextMonday() {
        // addWeek(1) ；即下周
        addWeek(1);
        // 设置为周一
        calendar.set(DAY_OF_WEEK, Week.Mon.ordinal() + 1);
        return this;
    }

    /**
     * 当天的开始时间
     * 设置为当天 0 时 0 分 0 秒
     *
     * @return {@link Dates}
     */
    public Dates beginTimeOfDay() {
        h(0).m(0).s(0).ms(0);
        return this;
    }

    /**
     * 当天的结束时间
     * 设置为当天 23 时 59 分 59 秒
     *
     * @return {@link Dates}
     */
    public Dates endTimeOfDay() {
        h(23).m(59).s(59).ms(999);
        return this;
    }

    /**
     * 设置为当月第一天
     *
     * @return {@link Dates}
     */
    public Dates firstDayOfMonth() {
        day(1);
        return this;
    }

    /**
     * 设置为当月最后一天
     *
     * @return {@link Dates}
     */
    public Dates lastDayOfMonth() {
        day(calendar.getActualMaximum(DAY_OF_MONTH));
        return this;
    }

    /**
     * 比对两个日期<br>
     * 小于 destDate 返回 -1；左小，右大；2018-01-01 | 2018-01-02=-1 <br>
     * 大于 destDate 返回 1； 右大，左小；2018-01-02 | 2018-01-01= 1<br>
     * 相等返回 0
     *
     * @param destDate Dates
     * @return int
     */
    public int compare(Dates destDate) {
        long src = this.get();
        long dest = destDate.get();
        if (src == dest) return 0;
        return (src < dest) ? -1 : 1;
    }

    /**
     * 比对两个日期，左边 > 右边
     *
     * @param destDate Dates
     * @return boolean
     */
    public boolean gt(Dates destDate) {
        return 1 == compare(destDate);
    }

    /**
     * 比对两个日期，左边 < 右边
     *
     * @param destDate Dates
     * @return boolean
     */
    public boolean lt(Dates destDate) {
        return -1 == compare(destDate);
    }

    /**
     * 比对两个日期，左边 >= 右边
     *
     * @param destDate Dates
     * @return boolean
     */
    public boolean ge(Dates destDate) {
        return -1 != compare(destDate);
    }

    /**
     * 比对两个日期，左边 <= 右边
     *
     * @param destDate Dates
     * @return boolean
     */
    public boolean le(Dates destDate) {
        return 1 != compare(destDate);
    }

    /**
     * 比对两个日期，左边 == 右边
     *
     * @param destDate Dates
     * @return boolean
     */
    public boolean eq(Dates destDate) {
        return 0 == compare(destDate);
    }

    /**
     * 获取时间间隔，单位：毫秒
     *
     * @return long
     */
    public long getTimeConsuming() {
        return System.currentTimeMillis() - this.get();
    }

    /**
     * 获取时间间隔，m分s秒
     *
     * @return String
     */
    public String getTimeConsumingText() {
        int second = (int) (getTimeConsuming() / 1000);
        int minute = Math.abs(second / 60);
        second = Math.abs(second % 60);
        StringBuilder sb = new StringBuilder();
        if (Math.abs(minute) > 0) sb.append(minute).append("分");
        if (second > 0) sb.append(second).append("秒");
        return sb.toString();
    }

    /**
     * 获取两个日期之间相差的天数
     * 目标日期destDate - 当前dates
     *
     * @param destDate Dates 目标日期
     * @return int 相差天数
     */
    public int getDifferDay(Dates destDate) {
        return (int) (destDate.get() - this.get() / (1000 * 60 * 60 * 24));
    }

    /**
     * 获取本年按季度划分的时间区间集合
     * 数据示例：[{"begin":"2017-01-01 00:00:00","end":"2017-03-31 23:59:59"}, {"begin":"2017-04-01 00:00:00","end":"2017-06-30 23:59:59"}, {"begin":"2017-07-01 00:00:00","end":"2017-09-30 23:59:59"}, {"begin":"2017-10-01 00:00:00","end":"2017-12-31 23:59:59"}]
     *
     * @return List<Range>
     */
    public List<Range> getRangeOfQuarter() {
        return Stream.of(
                new int[]{1, 3},
                new int[]{4, 6},
                new int[]{7, 9},
                new int[]{10, 12}
        )
                .map(arr -> Range.builder()
                        .begin(month(arr[0]).firstDayOfMonth().beginTimeOfDay().timestamp())
                        .end(month(arr[1]).lastDayOfMonth().endTimeOfDay().timestamp())
                        .build()
                )
                .collect(Collectors.toList());
    }

    /**
     * 获取当月时间区间
     * 数据示例：{"begin":"2017-01-01 00:00:00","end":"2017-1-31 23:59:59"}
     *
     * @return {@link Range}
     */
    public Range getRangeOfMonth() {
        return Range.builder()
                .begin(firstDayOfMonth().beginTimeOfDay().timestamp())
                .end(lastDayOfMonth().endTimeOfDay().timestamp())
                .build();
    }

    /**
     * 获取当年时间区间
     * 数据示例：{"begin":"2017-01-01 00:00:00","end":"2017-12-31 23:59:59"}
     *
     * @return {@link Range}
     */
    public Range getRangeOfYear() {
        return Range.builder()
                .begin(month(1).firstDayOfMonth().beginTimeOfDay().timestamp())
                .end(month(12).lastDayOfMonth().endTimeOfDay().timestamp())
                .build();
    }

    @Override
    public String toString() {
        return format("yyyy-MM-dd HH:mm:ss.SSS");
    }

    public static void main(String[] args) {
        log.info(Dates.now().format(Pattern.yyyy_MM));
        log.info(Dates.now().formatDateTime());
        log.info(Dates.now().formatDate());
        log.info(Dates.now().formatTime());
        log.info(Dates.of(new Date()).addYear(1).format("yyyy-MM-dd"));
        log.info(Dates.of(new Timestamp(new Date().getTime())).formatDateTime());
        log.info("{}", Dates.now().getRangeOfMonth());
        log.info("{}", Dates.now().getRangeOfYear());
        log.info("{}", Dates.now().getRangeOfQuarter());
        log.info("{}", JSON.parseObject("{\"begin\":\"2017-11-01\",\"end\":\"2017-11-30\"}", Range.class).rebuild());
        Dates dates = Dates.now();
        log.info(dates.formatDateTime());
        log.info("{}", dates.get());
        log.info("{}", Dates.of(dates.formatDateTime(), Pattern.yyyy_MM_dd_HH_mm_ss).get());
        log.info(Dates.of("2017-01-17 08:56:03 +0000", Pattern.yyyy_MM_dd).formatDate());

        log.info("左 > 右 true：{}", Dates.now().addDay(1).gt(Dates.now()));
        log.info("左 > 右 false：{}", Dates.now().gt(Dates.now().addDay(1)));
        log.info("左 < 右 true：{}", Dates.now().lt(Dates.now().addDay(1)));
        log.info("左 < 右 false：{}", Dates.now().addDay(1).lt(Dates.now()));
        log.info("左 = 右 true：{}", Dates.now().beginTimeOfDay().eq(Dates.now().beginTimeOfDay()));
        log.info("左 = 右 false：{}", Dates.now().addDay(1).beginTimeOfDay().eq(Dates.now().beginTimeOfDay()));
        log.info("左 >= 右 true：{}", Dates.now().beginTimeOfDay().ge(Dates.now().beginTimeOfDay()));
        log.info("左 >= 右 true：{}", Dates.now().addDay(1).beginTimeOfDay().ge(Dates.now().beginTimeOfDay()));
        log.info("左 >= 右 false：{}", Dates.now().beginTimeOfDay().ge(Dates.now().addDay(1).beginTimeOfDay()));
        log.info("左 <= 右 true：{}", Dates.now().beginTimeOfDay().le(Dates.now().beginTimeOfDay()));
        log.info("左 <= 右 true：{}", Dates.now().beginTimeOfDay().le(Dates.now().addDay(1).beginTimeOfDay()));
        log.info("左 <= 右 false：{}", Dates.now().addDay(1).beginTimeOfDay().le(Dates.now().beginTimeOfDay()));
        try { // UTC 日期
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            log.info("2018-02-01T01:25:04.226Z > {}", simpleDateFormat.parse("2018-02-01T01:25:04.226Z").getTime());
            log.info("2018-02-01T01:25:04.226Z > {}", Dates.of(simpleDateFormat.parse("2018-02-01T01:25:04.226Z").getTime()).formatDateTime());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

//        System.out.printf("now: %s%n", LocalDateTime.now());
//        System.out.printf("Apr 15, 1994 @ 11:30am: %s%n",
//                LocalDateTime.of(1994, Month.APRIL, 15, 11, 30));
//        System.out.printf("now (from Instant): %s%n",
//                LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
//        System.out.printf("6 months from now: %s%n",
//                LocalDateTime.now().plusMonths(6));
//        System.out.printf("6 months ago: %s%n",
//                LocalDateTime.now().minusMonths(6));

    }

//    /**
//     * 获取当前线程的decimalFormat工具类,因为该类是非同步的
//     * @param pattern 格式
//     * @return 该实例
//     */
//    private static DecimalFormat getDecimal(final String pattern){
//        ThreadLocal<DecimalFormat> instance = moneyMap.get(pattern);
//        if (instance == null){
//            synchronized (MoneyUtil.class){
//                instance = moneyMap.get(pattern);
//                if (instance == null){
//                    instance = new ThreadLocal<DecimalFormat>(){
//                        @Override
//                        protected DecimalFormat initialValue() {
//                            return new DecimalFormat(pattern);
//                        }
//                    };
//                }
//                moneyMap.put(pattern,instance);
//            }
//        }
//        return instance.get();
//    }

//    /**
//     * 返回一个ThreadLocal的sdf,每个线程只会new一次sdf
//     *
//     * @param pattern SimpleDateFormat规则
//     * @return 该实例
//     */
//    private static SimpleDateFormat getSdf(final String pattern){
//        ThreadLocal<SimpleDateFormat> t = sdfMap.get(pattern);
//        // 此处的双重判断和同步是为了防止sdfMap这个单例被多次put重复的sdf
//        if (t == null){
//            synchronized (DateUtil.class){
//                // 只有Map中还没有这个pattern的sdf才会生成新的sdf并放入map
//                logger.debug("put new sdf of pattern " + pattern + " to map");
//                // 这里是关键,使用ThreadLocal<SimpleDateFormat>替代原来直接new SimpleDateFormat
//                t = sdfMap.get(pattern);
//                if (t == null){
//                    t = new ThreadLocal<SimpleDateFormat>(){
//                        @Override
//                        protected SimpleDateFormat initialValue() {
//                            logger.debug("thread: " + Thread.currentThread() + " init pattern: " + pattern);
//                            return new SimpleDateFormat(pattern);
//                        }
//                    };
//                }
//                sdfMap.put(pattern,t);
//            }
//        }
//        return t.get();
//    }

}
