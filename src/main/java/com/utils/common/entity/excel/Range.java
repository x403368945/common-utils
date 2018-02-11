package com.utils.common.entity.excel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 定义excel 单元格区间
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

    public Range(String start, String end) {
        this.start = Position.of(start);
        this.end = Position.of(end);
    }

    /**
     * 单元格 起始 坐标 ： A1
     */
    private Position start;
    /**
     * 单元格 结束 坐标 ： B2
     */
    private Position end;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static void main(String[] args) {
        log.debug("{}", new Range("A1", "B2"));
        log.debug("{}", new Range("A1", "A10"));
        log.debug("{}", new Range(Position.of("B1"), Position.of("C2")));
        log.debug("{}", new Range(Position.of("B1"), Position.of("C20")));
    }
}
