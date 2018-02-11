package com.utils.common.entity.excel;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 定义excel 单元格坐标
 * @author Jason Xie on 2017/10/13.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Accessors(chain = true)
@Slf4j
public class Position {
    public static Position of(final String address) {
        Matcher m = Pattern.compile("^([A-Z]+)([0-9]+)$").matcher(address);
        return m.find() ? new Position(m.group(2), m.group(1)) : new Position();
    }
    public static Position ofRow(final String row) {
        return new Position(row, null);
    }
    public static Position ofColumn(final String column) {
        return new Position(null, column);
    }
    /**
     * 单元格 y 坐标 ：行 ： 0-9
     */
    private String row;
    /**
     * 单元格 x 坐标 ：列 ： A-Z
     */
    private String column;

    /**
     * 坐标位置文本：A11
     * @return String
     */
    public String address() {
        return column + row;
    }

    public int rowIndex() {
        return Integer.parseInt(row) - 1;
    }

    public int columnIndex() {
        int value = 0;
        for (Character c : column.toCharArray()) {
            value = 26 * value + c.hashCode() - 64;
        }
        return value - 1;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static void main(String[] args) {
        log.debug("{}",Position.of("A1"));
        log.debug("{}",Position.of("AB12"));
    }
}
