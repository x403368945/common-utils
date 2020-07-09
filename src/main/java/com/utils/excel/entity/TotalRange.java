package com.utils.excel.entity;

import com.alibaba.fastjson.annotation.JSONType;
import com.utils.IJson;
import com.utils.excel.Rownum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * 定义 excel 单元格区间，记录合计行行号及其子节点区间
 *
 * @author 谢长春 on 2017/10/13 .
 */
@NoArgsConstructor(staticName = "of")
@AllArgsConstructor(staticName = "of")
@Data
@Accessors(chain = true, fluent = true)
@JSONType(orders = {"total", "start", "end"})
@Slf4j
public class TotalRange implements IJson {

    private Rownum total;
    private Rownum begin;
    private Rownum end;

    public TotalRange totalNum(final int rownum) {
        this.total = Rownum.of(rownum);
        return this;
    }

    public TotalRange totalIndex(final int rowIndex) {
        this.total = Rownum.of(rowIndex + 1);
        return this;
    }

    public TotalRange beginNum(final int rownum) {
        this.begin = Rownum.of(rownum);
        return this;
    }

    public TotalRange beginIndex(final int rowIndex) {
        this.begin = Rownum.of(rowIndex + 1);
        return this;
    }

    public TotalRange endNum(final int rownum) {
        this.end = Rownum.of(rownum);
        return this;
    }

    public TotalRange endIndex(final int rowIndex) {
        this.end = Rownum.of(rowIndex + 1);
        return this;
    }

    public TotalRange forEach(final Consumer<Rownum> consumer) {
        for (int i = begin.get(); i <= end.get(); i++) {
            consumer.accept(Rownum.of(i));
        }
        return this;
    }

    public TotalRange forEachNum(final Consumer<Integer> consumer) {
        for (int i = begin.get(); i <= end.get(); i++) {
            consumer.accept(i);
        }
        return this;
    }

    public TotalRange forEachIndex(final Consumer<Integer> consumer) {
        for (int i = begin.index(); i <= end.index(); i++) {
            consumer.accept(i);
        }
        return this;
    }

    public static void main(String[] args) {
    }
}
