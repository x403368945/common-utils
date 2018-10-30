package com.utils.excel.enums;

import com.utils.excel.entity.Position;
import com.utils.excel.entity.Range;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 枚举：Excel 基本公式操作
 *
 * @author Jason Xie 2018/10/29
 */
public enum Formula {
    SUM("求和"),
    AVG("求平均值"),;

    final String comment;

    Formula(final String comment) {
        this.comment = comment;
    }

    /**
     * 按区间构建函数
     * SUM(A1:A10)
     * AVG(A1:B10)
     *
     * @param start {@link String:A1}
     * @param end   {@link String:A10}
     * @return {@link String}
     */
    public String of(final String start, final String end) {
        Objects.requireNonNull(start, "参数【start】是必须的");
        Objects.requireNonNull(end, "参数【end】是必须的");
        return String.format("%s(%s:%s)", this.name(), start, end);
    }

    /**
     * 按区间构建函数
     * SUM(A1:A10)
     * AVG(A1:B10)
     *
     * @param start {@link Position:A1}
     * @param end   {@link Position:A10}
     * @return {@link String}
     */
    public String of(final Position start, final Position end) {
        Objects.requireNonNull(start, "参数【start】是必须的");
        Objects.requireNonNull(end, "参数【end】是必须的");
        return String.format("%s(%s:%s)", this.name(), start.address(), end.address());
    }

    /**
     * 按区间构建函数
     * SUM(A1:A10)
     * AVG(A1:B10)
     *
     * @param range {@link Range:<start:A1, end:A10>}
     * @return {@link String}
     */
    public String of(final Range range) {
        Objects.requireNonNull(range.getStart(), "参数【start】是必须的");
        Objects.requireNonNull(range.getEnd(), "参数【end】是必须的");
        return String.format("%s(%s:%s)", this.name(), range.getStart().address(), range.getEnd().address());
    }

    /**
     * 按单元格坐标构建函数
     * SUM(A1,A2,A10)
     * AVG(A1,A2,B10)
     *
     * @param address {@link List<String:address:A1>}
     * @return {@link String}
     */
    public String of(final List<String> address) {
        Objects.requireNonNull(address, "参数【address】是必须的");
        return address.isEmpty()
                ? ""
                : String.format("%s(%s)", this.name(), address.stream().collect(Collectors.joining(",")));
    }

    /**
     * 按单元格坐标构建函数
     * SUM(A1,A2,A10)
     * AVG(A1,A2,B10)
     *
     * @param address {@link String[address:A1]}
     * @return {@link String}
     */
    public String of(final String... address) {
        return of(Arrays.asList(address));
    }
}
