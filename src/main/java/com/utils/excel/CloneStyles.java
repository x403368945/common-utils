package com.utils.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.model.StylesTable;

import java.util.HashMap;
import java.util.Objects;

/**
 * 样式库依赖对象
 *
 * @author 谢长春 on 2018-8-8.
 */
final class CloneStyles {
    CloneStyles(final StylesTable fromStyleTable, final Workbook toWorkbook) {
        this.toWorkbook = toWorkbook;
        this.fromStyleTable = fromStyleTable;
    }

    /**
     * 样式来源
     */
    private final StylesTable fromStyleTable;
    /**
     * 目标文档
     */
    private final Workbook toWorkbook;
    /**
     * 克隆样式缓存
     */
    private final HashMap<Integer, CellStyle> CACHE = new HashMap<>();

    CellStyle clone(int styleIndex) {
        if (Objects.isNull(fromStyleTable)) {
            return null;
        }
        if (!CACHE.containsKey(styleIndex)) { // 克隆并缓存样式，下次直接使用引用，不会重新创建样式
            CellStyle style = toWorkbook.createCellStyle();
            style.cloneStyleFrom(fromStyleTable.getStyleAt(styleIndex));
            CACHE.put(styleIndex, style);
//            CACHE.put((int) style.getIndex(), style); // 新产生的样式不能缓存
            return style;
        }
        return CACHE.get(styleIndex);
    }
}
