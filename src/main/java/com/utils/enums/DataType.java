package com.utils.enums;

import org.apache.poi.ss.usermodel.CellType;

/**
 * EXCEL数据类型定义
 */
public enum DataType {
    /** 数值*/
    Number("数值", CellType.NUMERIC),
    /** 文本*/
    Text("文本", CellType.STRING),
    /** 日期*/
    Date("日期", CellType.NUMERIC),
    /** 百分比*/
    Percent("百分比", CellType.NUMERIC),
    ;
    /**
     * 枚举属性说明
     */
    final String comment;
    public final CellType cellType;

    DataType(String comment, CellType cellType) {
        this.comment = comment;
        this.cellType = cellType;
    }
}
