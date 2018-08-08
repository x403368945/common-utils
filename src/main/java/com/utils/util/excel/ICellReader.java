package com.utils.util.excel;

import com.utils.enums.DataType;
import com.utils.util.Dates;
import com.utils.util.Num;
import org.apache.poi.ss.usermodel.*;

import java.util.Objects;
import java.util.Optional;
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
     * 判断单元格是否非空，对 {@link ICellReader#cellEmpty} 取反
     *
     * @return boolean true：非空，false：空
     */
    default boolean cellNotEmpty() {
        return !cellEmpty();
    }

    /**
     * 判断单元格是否为空，cell对象不存在 或者 单元格类型为CellType.BLANK，表示单元格为空
     *
     * @return boolean true：空，false：非空
     */
    default boolean cellEmpty() {
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
        if (cellEmpty()) return Optional.empty();
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
    default String text() {
        return value().map(v -> {
            if (v instanceof Double) return Num.of(v).toBigDecimal().toPlainString(); // 解决科学计数法 toString()问题
            else return v.toString();
        }).orElse(null);
    }

    /**
     * 获取单元格文本，null值默认为空字符串 ""
     *
     * @return String
     */
    default String textOfEmpty() {
        return Optional.ofNullable(text()).orElse("");
    }

    /**
     * 获取单元格数值，空值和非数字默认为null
     *
     * @return {@link Num}
     */
    default Num number() {
        return value().map(Num::of).orElse(null);
    }

    /**
     * 获取单元格数值，空值和非数字默认为0
     *
     * @return {@link Num}
     */
    default Num numberOfZore() {
        return value().map(v -> Num.of(v.toString(), 0)).orElse(Num.of(0));
    }

    /**
     * 获取单元格日期对象
     *
     * @return {@link Dates}
     */
    default Dates date() {
        return value().map(v -> Num.of(v.toString()).toDate()).orElse(null);
    }

    /**
     * 获取公式 不使用占位符替换行号
     *
     * @return String
     */
    default String formula() {
        return formula(null);
    }

    /**
     * 获取公式
     *
     * @param rowIndex Supplier<Integer> 获取行索引，行索引+1获得公式中间的行号，将行号使用 {0} 占位
     * @return String
     */
    default String formula(Supplier<Integer> rowIndex) {
        if (cellEmpty()) return null;
        if (Objects.equals(CellType.FORMULA, getCell().getCellTypeEnum()))
            return Objects.nonNull(rowIndex)
                    ? getCell().getCellFormula().replaceAll("(?<=[A-Z])" + (rowIndex.get() + 1), "{0}") // 获取到的公式将会使用正则替换为行占位符
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
}
