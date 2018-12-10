package com.utils.util;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 数字区间操作类
 *
 * @author Jason Xie 2018/12/10
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JSONType(orders = {"min", "max"})
@Slf4j
public class Range<T extends Number> implements Num.IRange<T> {
    private T min;
    private T max;

    public static <T extends Number> Range<T> of(final T min, final T max) {
        if (max.doubleValue() <= 0) {
            log.warn("参数【max】<=0");
        }
        final Range<T> range = new Range<>();
        range.setMin(min);
        range.setMax(max);
        return range;
    }
}