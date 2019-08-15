package com.utils.excel.entity;

import com.alibaba.fastjson.annotation.JSONType;
import com.utils.IJson;
import com.utils.excel.Rownum;
import lombok.Data;

import java.util.List;

/**
 * Excel 写入行配置
 *
 * @author 谢长春 2019/6/18
 */
@Data
@JSONType(orders = {"row", "from", "comment", "columns"})
public class RowConfig implements IJson {
    /**
     * 行号：一般只有明确往哪一行写入时，才指定该参数
     */
    private Integer row;
    /**
     * 模板行来源
     */
    private Integer from;
    /**
     * 说明
     */
    private String comment;
    /**
     * 列配置信息
     */
    private List<Cell> columns;

    public Rownum row() {
        return Rownum.of(row);
    }

    public Rownum from() {
        return Rownum.of(from);
    }

    @Override
    public String toString() {
        return json();
    }

}
