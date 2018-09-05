package com.utils.util.excel;

import com.utils.enums.DataType;
import com.utils.util.Dates;
import com.utils.util.Num;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Cell单元格读取操作
 *
 * @author Jason Xie on 2018-8-8.
 */
public interface ICellReader {
    /**
     * 获取当前操作单元格
     *
     * @return {@link Cell}
     */
    Cell getCell();

    /**
     * 读取单元格格式化数据时，需要使用数据格式化执行器
     *
     * @return {@link DataFormatter}
     */
    DataFormatter getDataFormatter();

    /**
     * 判断单元格是否非空，对 {@link ICellReader#cellIsNull} 取反
     *
     * @return boolean true：非空，false：空
     */
    default boolean cellNotNull() {
        return !cellIsNull();
    }

    /**
     * 判断单元格是否为空，cell对象不存在 或者 单元格类型为CellType.BLANK，表示单元格为空
     *
     * @return boolean true：空，false：非空
     */
    default boolean cellIsNull() {
        return Objects.isNull(getCell());
    }

    /**
     * 判断单元格是否非空，对 {@link ICellReader#cellIsBlank} 取反
     *
     * @return boolean true：非空，false：空
     */
    default boolean cellNotBlank() {
        return !cellIsBlank();
    }

    /**
     * 判断单元格是否为空，cell对象不存在 或者 单元格类型为CellType.BLANK，表示单元格为空
     *
     * @return boolean true：空，false：非空
     */
    default boolean cellIsBlank() {
        return Objects.isNull(getCell()) || Objects.equals(CellType.BLANK, getCell().getCellTypeEnum());
    }

    /**
     * 返回单元格数据原始值
     *
     * @return Optional<Object>
     */
    default Optional<Object> value() {
        return value(false);
    }

    /**
     * 获取单元格数据
     *
     * @param format boolean 返回时是否格式化单元格数据；true：是，false：否
     * @return Optional<Object>
     */
    default Optional<Object> value(final boolean format) {
        if (cellIsBlank()) return Optional.empty();
        switch (getCell().getCellTypeEnum()) {
            case STRING:
                return Optional.of(getCell().getStringCellValue());
            case NUMERIC:
                if (format) {
                    final CellStyle style = getCell().getCellStyle();
                    final String formatPattern = Optional
                            .ofNullable(getCell().getCellStyle().getDataFormatString())
                            .map(v -> "".equals(v) ? null : v)
                            .orElse(BuiltinFormats.getBuiltinFormat(style.getDataFormat()));
                    return Optional.of(getDataFormatter().formatRawCellContents(getCell().getNumericCellValue(), style.getDataFormat(), formatPattern));
                }
                return Optional.of(DateUtil.isCellDateFormatted(getCell()) ? getCell().getDateCellValue().getTime() : getCell().getNumericCellValue());
            case BOOLEAN:
                return Optional.of(getCell().getBooleanCellValue());
            case FORMULA:
                if (format) {
                    final CellStyle style = getCell().getCellStyle();
                    final String formatPattern = Optional
                            .ofNullable(getCell().getCellStyle().getDataFormatString())
                            .map(v -> "".equals(v) ? null : v)
                            .orElse(BuiltinFormats.getBuiltinFormat(style.getDataFormat()));
                    return Optional.of(new DataFormatter().formatRawCellContents(getCell().getNumericCellValue(), style.getDataFormat(), formatPattern));
                }
                // Cell.getCachedFormulaResultTypeEnum() 可以判断公式计算结果得出的数据类型；前置条件必须是 Cell.getCellTypeEnum() = CellType.FORMULA
                switch (getCell().getCachedFormulaResultTypeEnum()) {
                    case _NONE:
                        break;
                    case NUMERIC:
                        return Optional.of(getCell().getNumericCellValue());
                    case STRING:
                        return Optional.of(getCell().getStringCellValue());
                    case FORMULA:
                        break;
                    case BLANK:
                        break;
                    case BOOLEAN:
                        break;
                    case ERROR:
                        break;
                }
        }
        getCell().setCellType(CellType.STRING);
        return Optional.of(getCell().getStringCellValue());
    }

    /**
     * 获取单元格文本，保留null值
     *
     * @return String
     */
    default String stringValue() {
        if (cellIsBlank()) return null;
        switch (getCell().getCellTypeEnum()) {
            case STRING:
                return getCell().getStringCellValue();
            case NUMERIC:
                return (DateUtil.isCellDateFormatted(getCell()))
                        ? Dates.of(getCell().getDateCellValue().getTime()).format(Dates.Pattern.yyyy_MM_dd_HH_mm_ss)
                        : Num.of(getCell().getNumericCellValue()).toBigDecimal().toPlainString(); // 解决科学计数法 toString()问题
            case BOOLEAN:
                return Objects.toString(getCell().getBooleanCellValue());
            case FORMULA:
                // Cell.getCachedFormulaResultTypeEnum() 可以判断公式计算结果得出的数据类型；前置条件必须是 Cell.getCellTypeEnum() = CellType.FORMULA
                switch (getCell().getCachedFormulaResultTypeEnum()) {
                    case NUMERIC:
                        return Objects.toString(getCell().getNumericCellValue());
                    case _NONE:
                    case STRING:
                    case FORMULA:
                    case BLANK:
                    case BOOLEAN:
                    case ERROR:
                }
        }
        getCell().setCellType(CellType.STRING);
        return getCell().getStringCellValue();
    }

    /**
     * 获取单元格文本，null值默认为空字符串 ""
     *
     * @return String
     */
    default String stringOfEmpty() {
        return Optional.ofNullable(stringValue()).orElse("");
    }

    /**
     * 获取单元格数值，空值和非数字默认为null
     *
     * @return {@link Num}
     */
    default Num numberValue() {
        if (cellIsBlank()) return null;
        switch (getCell().getCellTypeEnum()) {
            case STRING:
                return Num.of(getCell().getStringCellValue());
            case NUMERIC:
                return Num.of(DateUtil.isCellDateFormatted(getCell())
                        ? getCell().getDateCellValue().getTime()
                        : getCell().getNumericCellValue()); // 解决科学计数法 toString()问题
            case BOOLEAN:
                return Num.of(getCell().getBooleanCellValue() ? 1 : 0);
            case FORMULA:
                // Cell.getCachedFormulaResultTypeEnum() 可以判断公式计算结果得出的数据类型；前置条件必须是 Cell.getCellTypeEnum() = CellType.FORMULA
                switch (getCell().getCachedFormulaResultTypeEnum()) {
                    case NUMERIC:
                        return Num.of(getCell().getNumericCellValue());
                    case _NONE:
                    case STRING:
                    case FORMULA:
                    case BLANK:
                    case BOOLEAN:
                    case ERROR:
                }
        }
        getCell().setCellType(CellType.NUMERIC);
        return Num.of(getCell().getNumericCellValue());
    }

    /**
     * 获取单元格数值，空值和非数字默认为0
     *
     * @return {@link Num}
     */
    default Num numberOfZore() {
        return Optional.ofNullable(numberValue()).orElse(Num.of(0));
    }

    /**
     * 获取单元格日期对象
     *
     * @return {@link Dates}
     */
    default Dates dateValue() {
//        return value().map(v -> Num.of(v.toString()).toDate()).orElse(null);
        return (cellNotBlank() && DateUtil.isCellDateFormatted(getCell()))
                ? Dates.of(getCell().getDateCellValue().getTime())
                : null;
    }

    /**
     * 获取公式 不使用占位符替换行号
     *
     * @return String
     */
    default String formula() {
        return formula(() -> getCell().getRowIndex());
    }

    /**
     * 获取公式
     *
     * @param rowIndex Supplier<Integer> 获取行索引，行索引+1获得公式中间的行号，将行号使用 {0} 占位
     * @return String
     */
    default String formula(Supplier<Integer> rowIndex) {
        if (cellIsBlank()) return null;
        if (Objects.equals(CellType.FORMULA, getCell().getCellTypeEnum()))
            return Objects.nonNull(rowIndex)
                    ? getCell().getCellFormula().replaceAll(String.format("(?<=[A-Z])%d", rowIndex.get() + 1), "{0}") // 获取到的公式将会使用正则替换为行占位符
                    : getCell().getCellFormula();
        return null;
    }

    /**
     * 获取样式索引
     *
     * @return Integer
     */
    default Integer sindex() {
        return Objects.isNull(getCell()) ? null : (int) getCell().getCellStyle().getIndex();
    }

    /**
     * 获取格式化之后的字符串
     *
     * @return String
     */
    default String dataFormat() {
        return Objects.isNull(getCell()) ? null : getCell().getCellStyle().getDataFormatString();
    }

    /**
     * 获取单元格数据类型
     *
     * @return {@link DataType}
     */
    default DataType type() {
        if (Objects.isNull(getCell())) return null;
        switch (getCell().getCellTypeEnum()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(getCell())) return DataType.DATE;
                else if (Optional.ofNullable(dataFormat()).orElse("").endsWith("%")) return DataType.PERCENT;
                else return DataType.NUMBER;
            case FORMULA:
                if (CellType.NUMERIC == getCell().getCachedFormulaResultTypeEnum()) return DataType.NUMBER;
        }
        return DataType.TEXT;
    }

    /**
     * 获取单元格数据类型
     *
     * @return {@link CellType}
     */
    default CellType cellType() {
        if (Objects.isNull(getCell())) return null;
        return getCell().getCellTypeEnum();
    }
}
