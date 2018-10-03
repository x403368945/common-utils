package com.utils.excel.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * 定义excel 单元格区间
 *
 * @author Jason Xie on 2017/10/13.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Accessors(chain = true)
@JSONType(orders = {"start", "end"})
@Slf4j
public class Range {
    public static Range of(final String range) {
        if (!range.matches("^[A-Z]+\\d+:[A-Z]+\\d+$")) throw new IllegalArgumentException("区间值不正确");
        final String[] arrs = range.split(":");
        return new Range(Position.of(arrs[0]), Position.of(arrs[1]));
    }

    public static Range of(final String start, final String end) {
        return new Range(Position.of(start), Position.of(end));
    }

    /**
     * 单元格 起始 坐标 ： A1
     */
    private Position start;
    /**
     * 单元格 结束 坐标 ： B2
     */
    private Position end;

    public CellRangeAddress getCellRangeAddress() {
        return new CellRangeAddress(
                start.rowIndex(),
                end.rowIndex(),
                start.columnIndex(),
                end.columnIndex()
        );
    }
    public CellRangeAddressList getCellRangeAddressList() {
        return new CellRangeAddressList(
                start.rowIndex(),
                end.rowIndex(),
                start.columnIndex(),
                end.columnIndex()
        );
    }

    /**
     * 遍历选定区间
     *
     * @param action {@link BiConsumer<Integer:rowIndex, Integer:columnIndex>}
     */
    public void forEach(final BiConsumer<Integer, Integer> action) {
        Objects.requireNonNull(action, "参数【action】是必须的");
        for (int row = start.rowIndex(); row <= end.rowIndex(); row++) {
            for (int column = start.columnIndex(); column <= end.columnIndex(); column++) {
                action.accept(row, column);
            }
        }
    }

    /**
     * 转换选定区间，行索引和列索引归集为数组
     *
     * @param mapper {@link BiFunction<Integer:rowIndex, Integer:columnIndex, R:返回数据类型>}
     * @param <R>    返回数据类型
     * @return {@link Stream<R>}
     */
    public <R> Stream<R> map(final BiFunction<Integer, Integer, ? extends R> mapper) {
        final List<R> list = new ArrayList<>();
        for (int row = start.rowIndex(); row < end.rowIndex(); row++) {
            for (int column = start.columnIndex(); column < end.columnIndex(); column++) {
                list.add(mapper.apply(row, column));
            }
        }
        return list.stream();
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static void main(String[] args) {
        log.info("{}", Range.of("B1:C2"));
        log.info("{}", Range.of("B1:C20"));
        log.info("{}", Range.of("A1", "B2"));
        log.info("{}", Range.of("A1", "A10"));
        log.info("{}", new Range(Position.of("B1"), Position.of("C2")));
        log.info("{}", new Range(Position.of("B1"), Position.of("C20")));
    }
}
