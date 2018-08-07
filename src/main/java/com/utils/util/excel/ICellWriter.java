package com.utils.util.excel;

import com.utils.enums.DataType;
import com.utils.util.Util;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Excel 单元格写入
 */
public interface ICellWriter<T extends ICellWriter> {
    /**
     * 获取当前操作单元格
     * @return {@link Cell}
     */
    Cell getCell();

    /**
     * 获取克隆源样式
     * @return {@link CloneStyles}
     */
    CloneStyles getCloneStyles();

    /**
     * 向当前单元格写入数据
     *
     * @param data Cell 写入数据单元格对象
     * @return <T extends ISheetWriter>
     */
    default T write(final com.utils.common.entity.excel.Cell data) {
        writeStyle(data.getSindex()); // 必须先指定样式库（StylesTable），这里写入才有效
        if (Objects.isNull(data)) {
            getCell().setCellType(CellType.BLANK);
            return (T) this;
        }
        if (Objects.nonNull(data.getFormula())) {
            writeFormula(data.getFormula());
            return (T) this; // 如果单元格有公式，则写完公式就跳出，所以需要提前return (T) this;
        }
        if (Objects.isNull(data.getType())) data.setType(DataType.TEXT);
        switch (data.getType()) {
            case DATE:
                if (Objects.nonNull(data.getValue())) writeDate(data.getDate().date());
                break;
            case NUMBER:
            case PERCENT:
                if (Objects.nonNull(data.getValue())) writeNumber(data.getNumber().doubleValue());
                break;
            default:
                writeText(data.getText());
        }
        return (T) this;
    }

    /**
     * 向当前行单元格写入文本内容
     *
     * @param value String 文本内容
     * @return <T extends ISheetWriter>
     */
    default T writeText(final String value) {
        if (Objects.isNull(value)) getCell().setCellType(CellType.BLANK);
        else {
            getCell().setCellType(CellType.STRING);
            getCell().setCellValue(value);
        }
        return (T) this;
    }

    /**
     * 向当前单元格写入数字
     *
     * @param value Number 数字
     * @return <T extends ISheetWriter>
     */
    default T writeNumber(final Number value) {
        if (Objects.isNull(value)) getCell().setCellType(CellType.BLANK); // 设置为 BLANK 可以清空单元格内容，保留样式
        else {
            getCell().setCellType(CellType.NUMERIC);
            getCell().setCellValue(value.doubleValue());
        }
        return (T) this;
    }

    /**
     * 向当前单元格写入日期，使用此方法必须通过 writeStyle 写入日期格式化样式
     *
     * @param value Date 日期
     * @return <T extends ISheetWriter>
     */
    default T writeDate(final Date value) {
        if (Objects.isNull(value)) getCell().setCellType(CellType.BLANK); // 设置为 BLANK 可以清空单元格内容，保留样式
        else getCell().setCellValue(value);
        return (T) this;
    }

    /**
     * 向当前单元格写入日期，使用此方法必须通过 writeStyle 写入日期格式化样式
     *
     * @param value Timestamp 日期
     * @return <T extends ISheetWriter>
     */
    default T writeDate(final Timestamp value) {
        if (Objects.isNull(value)) getCell().setCellType(CellType.BLANK); // 设置为 BLANK 可以清空单元格内容，保留样式
        else getCell().setCellValue(value);
        return (T) this;
    }

    /**
     * 向当前单元格写入日期
     *
     * @param value String 日期
     * @return <T extends ISheetWriter>
     */
    default T writeDate(final String value) {
        if (Objects.isNull(value)) getCell().setCellType(CellType.BLANK); // 设置为 BLANK 可以清空单元格内容，保留样式
        else getCell().setCellValue(value);
        return (T) this;
    }

    /**
     * 向当前单元格写入公式
     *
     * @param formula String 公式
     * @return <T extends ISheetWriter>
     */
    default T writeFormula(final String formula) {
        if (Util.isEmpty(formula)) getCell().setCellType(CellType.BLANK);
        else getCell().setCellFormula(formula);
        return (T) this;
    }
    /**
     * 向当前单元格写入公式
     *
     * @param formula Supplier<String> 公式构造器
     * @return <T extends ISheetWriter>
     */
    default T writeFormula(final Supplier<String> formula) {
//        final Supplier<String> supplier = () -> { // 获取公式
//            if (formula.indexOf("{0}") > 0) { // 当公式使用 {0} 占位行号时，将 {0} 替换成行号
//                return formula.replace("{0}", (getRow().getRowNum() + 1) + "");
//            } else if (getOps().rebuildFormula) { // 判断如果开启公式重构，则执行公式重构方法
//                // 重构规则说明：假设当前行号为100
//                // 公式：A1+B1 > A100+B100
//                // 公式：SUM(A1:C1) > SUM(A100:C100)
//                // 公式：A1*C1 > A100*C100
//                // 公式：A1*C1-D1 > A100*C100-D100
//                // 公式(错误案例演示)：A1+A2+A3 > A100+A100+A100；因为：A1+A2+A3 属于跨行计算
//                // 公式(错误案例演示)：SUM(A1:A3) > SUM(A100:A100)；因为：A1:A3 属于跨行计算
//                // 以上案例说明，只支持横向的单行公式，不支持跨行和跨表
//                return formula.replaceAll("(((?<=.?[A-Z])(\\d{1,10})(?=\\D?.*))|(?<=.?[A-Z])(\\d{1,10})$)", (getRow().getRowNum() + 1) + "");
//            }
//            return formula;
//        };
        if (Util.isEmpty(formula)) getCell().setCellType(CellType.BLANK);
        else getCell().setCellFormula(formula.get());
        return (T) this;
    }

    /**
     * 向当前单元格写入样式
     *
     * @param styleIndex int 样式索引，将会从样式库中获取样式
     * @return <T extends ISheetWriter>
     */
    default T writeStyle(final int styleIndex) {
        writeStyle(getCloneStyles().clone(styleIndex));
        return (T) this;
    }

    /**
     * 向当前单元格写入样式
     *
     * @param cellStyle CellStyle 样式
     * @return <T extends ISheetWriter>
     */
    default T writeStyle(final CellStyle cellStyle) {
        if (Objects.nonNull(cellStyle)) getCell().setCellStyle(cellStyle);
        return (T) this;
    }
}
