package com.utils.excel;

import com.utils.excel.enums.DataType;
import com.utils.util.Dates;
import com.utils.util.Num;
import org.apache.poi.ss.usermodel.*;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Cell单元格读取操作
 *
 * @author 谢长春 on 2018-8-8 .
 */
public interface ICellReader<T extends ICellReader> {
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
        return cellIsNull() || Objects.equals(CellType.BLANK, getCell().getCellType());
    }

    /**
     * 获取单元格数据
     *
     * @param format boolean 返回时是否格式化单元格数据；true：是，false：否
     * @return Optional<Object>
     */
    default Optional<Object> value(final boolean format) {
        if (cellIsBlank()) {
            return Optional.empty();
        }
        switch (getCell().getCellType()) {
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
                switch (getCell().getCachedFormulaResultType()) {
                    case NUMERIC:
                        return Optional.of(getCell().getNumericCellValue());
                    case STRING:
                        return Optional.of(getCell().getStringCellValue());
                    case BOOLEAN:
                        return Optional.of(getCell().getBooleanCellValue());
                    case _NONE:
                    case BLANK:
                    case ERROR:
                        return Optional.empty();
                    case FORMULA:
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return Optional.of(getCell().getStringCellValue());
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
     * 获取单元格数据原始值，consumer里面参数是Optional，可以通过 .orElse() 指定默认值
     *
     * @param consumer {@link Consumer}{@link Consumer<Optional<Object>>} 处理单元格数据
     * @return <T extends ICellReader>
     */
    default T value(final Consumer<Optional<Object>> consumer) {
        consumer.accept(value());
        return (T) this;
    }

    /**
     * 获取单元格文本，保留null值
     *
     * @return String
     */
    default String stringValue() {
        if (cellIsBlank()) {
            return null;
        }
        switch (getCell().getCellType()) {
            case STRING:
                return getCell().getStringCellValue();
            case NUMERIC:
                return (DateUtil.isCellDateFormatted(getCell()))
                        ? Dates.of(getCell().getDateCellValue().getTime()).format(Dates.Pattern.yyyy_MM_dd_HH_mm_ss)
                        : Num.of(getCell().getNumericCellValue()).toBigDecimal().toPlainString(); // 解决科学计数法 toString()问题
//                        : Num.of(getCell().getNumericCellValue()).toBigDecimal().setScale(4, ROUND_HALF_UP).toPlainString(); // 解决科学计数法 toString()问题
//                        : Optional.ofNullable(Num.of(getCell().getNumericCellValue()).toBigDecimal()).map(bigDecimal -> bigDecimal.setScale(4, ROUND_HALF_UP).toPlainString() /* 解决科学计数法 toString()问题*/).orElse(null);
            case BOOLEAN:
                return Objects.toString(getCell().getBooleanCellValue(), null);
            case FORMULA:
                // Cell.getCachedFormulaResultTypeEnum() 可以判断公式计算结果得出的数据类型；前置条件必须是 Cell.getCellTypeEnum() = CellType.FORMULA
                switch (getCell().getCachedFormulaResultType()) {
                    case NUMERIC:
                        return Objects.toString(getCell().getNumericCellValue(), null);
                    case STRING:
                        return getCell().getStringCellValue();
                    case BOOLEAN:
                        return Objects.toString(getCell().getBooleanCellValue(), null);
                    case _NONE:
                    case BLANK:
                    case ERROR:
                        return null;
                    case FORMULA:
                    default:
                        break;
                }
                break;
            default:
                break;
        }
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
     * 获取单元格文本，consumer里面参数是Optional，可以通过 .orElse() 指定默认值
     *
     * @param consumer {@link Consumer}{@link Consumer<Optional<String>>} 处理单元格数据
     * @return <T extends ICellReader>
     */
    default T stringValue(final Consumer<Optional<String>> consumer) {
        consumer.accept(Optional.ofNullable(stringValue()));
        return (T) this;
    }

    /**
     * 获取单元格数值，空值和非数字默认为null
     *
     * @return {@link Num}
     */
    default Num numberValue() {
        if (cellIsBlank()) {
            return null;
        }
        switch (getCell().getCellType()) {
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
                switch (getCell().getCachedFormulaResultType()) {
                    case NUMERIC:
                        return Num.of(getCell().getNumericCellValue());
                    case STRING:
                        return Num.of(getCell().getStringCellValue());
                    case BOOLEAN:
                        return Num.of(getCell().getBooleanCellValue() ? 1 : 0);
                    case _NONE:
                    case BLANK:
                    case ERROR:
                        return null;
                    case FORMULA:
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return Num.of(getCell().getNumericCellValue());
    }

    /**
     * 获取单元格数值，consumer里面参数是Optional，可以通过 .orElse() 指定默认值
     *
     * @param consumer {@link Consumer}{@link Consumer<Optional<Num>>} 处理单元格数据
     * @return <T extends ICellReader>
     */
    default T numberValue(final Consumer<Optional<Num>> consumer) {
        consumer.accept(Optional.ofNullable(numberValue()));
        return (T) this;
    }

    /**
     * 获取单元格数值，空值和非数字默认为0
     *
     * @return {@link Num}
     */
    default Num numberOfZero() {
        return Optional.ofNullable(numberValue()).orElse(Num.of(0));
    }

    /**
     * 获取单元格数值，空值和非数字默认为null
     *
     * @return {@link Integer}
     */
    default Integer intValue() {
        return Optional.ofNullable(numberValue()).map(Num::toInteger).orElse(null);
    }

    /**
     * 获取单元格数值，consumer里面参数是Optional，可以通过 .orElse() 指定默认值
     *
     * @param consumer {@link Consumer}{@link Consumer<Optional<Integer>>} 处理单元格数据
     * @return <T extends ICellReader>
     */
    default T intValue(final Consumer<Optional<Integer>> consumer) {
        consumer.accept(Optional.ofNullable(numberValue()).map(Num::toInteger));
        return (T) this;
    }

    /**
     * 获取单元格数值，空值和非数字默认为0
     *
     * @return int
     */
    default int intValueOfZero() {
        return Optional.ofNullable(numberValue()).map(Num::intValue).orElse(0);
    }

    /**
     * 获取单元格数值，空值和非数字默认为null
     *
     * @return {@link Short}
     */
    default Short shortValue() {
        return Optional.ofNullable(numberValue()).map(Num::toShort).orElse(null);
    }

    /**
     * 获取单元格数值，consumer里面参数是Optional，可以通过 .orElse() 指定默认值
     *
     * @param consumer {@link Consumer}{@link Consumer<Optional<Short>>} 处理单元格数据
     * @return <T extends ICellReader>
     */
    default T shortValue(final Consumer<Optional<Short>> consumer) {
        consumer.accept(Optional.ofNullable(numberValue()).map(Num::toShort));
        return (T) this;
    }

    /**
     * 获取单元格数值，空值和非数字默认为0
     *
     * @return short
     */
    default short shortValueOfZero() {
        return Optional.ofNullable(numberValue()).map(Num::shortValue).orElse((short) 0);
    }

    /**
     * 获取单元格数值，空值和非数字默认为null
     *
     * @return {@link Long}
     */
    default Long longValue() {
        return Optional.ofNullable(numberValue()).map(Num::toLong).orElse(null);
    }

    /**
     * 获取单元格数值，consumer里面参数是Optional，可以通过 .orElse() 指定默认值
     *
     * @param consumer {@link Consumer}{@link Consumer<Optional<Long>>} 处理单元格数据
     * @return <T extends ICellReader>
     */
    default T longValue(final Consumer<Optional<Long>> consumer) {
        consumer.accept(Optional.ofNullable(numberValue()).map(Num::toLong));
        return (T) this;
    }

    /**
     * 获取单元格数值，空值和非数字默认为0
     *
     * @return long
     */
    default long longValueOfZero() {
        return Optional.ofNullable(numberValue()).map(Num::longValue).orElse(0L);
    }

    /**
     * 获取单元格数值，空值和非数字默认为null
     *
     * @return {@link Double}
     */
    default Double doubleValue() {
        return Optional.ofNullable(numberValue()).map(Num::toDouble).orElse(null);
    }

    /**
     * 获取单元格数值，consumer里面参数是Optional，可以通过 .orElse() 指定默认值
     *
     * @param consumer {@link Consumer}{@link Consumer<Optional<Double>>} 处理单元格数据
     * @return <T extends ICellReader>
     */
    default T doubleValue(final Consumer<Optional<Double>> consumer) {
        consumer.accept(Optional.ofNullable(numberValue()).map(Num::toDouble));
        return (T) this;
    }

    /**
     * 获取单元格数值，空值和非数字默认为0
     *
     * @return double
     */
    default double doubleValueOfZero() {
        return Optional.ofNullable(numberValue()).map(Num::doubleValue).orElse(0D);
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
     * 获取单元格日期对象，consumer里面参数是Optional，可以通过 .orElse() 指定默认值
     *
     * @param consumer {@link Consumer}{@link Consumer<Optional<Dates>>} 处理单元格数据
     * @return <T extends ICellReader>
     */
    default T dateValue(final Consumer<Optional<Dates>> consumer) {
        consumer.accept(Optional.ofNullable(dateValue()));
        return (T) this;
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
     * 获取公式 不使用占位符替换行号，consumer里面参数是Optional，可以通过 .orElse() 指定默认值
     *
     * @param consumer {@link Consumer}{@link Consumer<Optional<Dates>>} 处理单元格公式
     * @return <T extends ICellReader>
     */
    default T formula(final Consumer<Optional<String>> consumer) {
        consumer.accept(Optional.ofNullable(formula()));
        return (T) this;
    }

    /**
     * 获取公式
     *
     * @param rowIndex {@link Supplier}{@link Supplier<Integer:rowIndex>} 获取行索引,行索引+1获得公式中间的行号，将行号使用 {0} 占位
     * @return String
     */
    default String formula(final Supplier<Integer> rowIndex) {
        if (cellIsBlank()) {
            return null;
        }
        if (Objects.equals(CellType.FORMULA, getCell().getCellType())) {
            return Objects.nonNull(rowIndex)
                    ? getCell().getCellFormula().replaceAll(String.format("(?<=[A-Z])%d", rowIndex.get() + 1), "{0}") // 获取到的公式将会使用正则替换为行占位符
                    : getCell().getCellFormula();
        }
        return null;
    }

    /**
     * 获取公式，consumer里面参数是Optional，可以通过 .orElse() 指定默认值
     *
     * @param rowIndex {@link Supplier}{@link Supplier<Integer:rowIndex>} 获取行索引,行索引+1获得公式中间的行号，将行号使用 {0} 占位
     * @param consumer {@link Consumer}{@link Consumer<Optional<String>>} 处理单元格公式
     * @return <T extends ICellReader>
     */
    default T formula(final Supplier<Integer> rowIndex, final Consumer<Optional<String>> consumer) {
        consumer.accept(Optional.ofNullable(formula(rowIndex)));
        return (T) this;
    }

    /**
     * 获取单元格批注
     *
     * @return String
     */
    default String comment() {
        return Optional
                .ofNullable(cellIsNull() ? null : getCell().getCellComment())
                .map(comment -> comment.getString().getString())
                .orElse(null);
    }

    /**
     * 获取单元格批注，consumer里面参数是Optional，可以通过 .orElse() 指定默认值
     *
     * @param consumer {@link Consumer}{@link Consumer<Optional<String>>} 处理单元格批注
     * @return <T extends ICellReader>
     */
    default T comment(final Consumer<Optional<String>> consumer) {
        consumer.accept(Optional.ofNullable(comment()));
        return (T) this;
    }

    /**
     * 获取样式索引
     *
     * @return Integer
     */
    default Integer sindex() {
        return cellIsNull() ? null : (int) getCell().getCellStyle().getIndex();
    }

    /**
     * 获取样式索引
     *
     * @param consumer {@link Consumer}{@link Consumer<Integer>} 处理单元格批注
     * @return <T extends ICellReader>
     */
    default T sindex(final Consumer<Integer> consumer) {
        consumer.accept(sindex());
        return (T) this;
    }

    /**
     * 获取字符串格式化表达式
     *
     * @return String
     */
    default String dataFormat() {
        return cellIsNull() ? null : getCell().getCellStyle().getDataFormatString();
    }

    /**
     * 获取字符串格式化表达式
     *
     * @param consumer {@link Consumer}{@link Consumer<String>} 处理单元格批注
     * @return <T extends ICellReader>
     */
    default T dataFormat(final Consumer<String> consumer) {
        consumer.accept(dataFormat());
        return (T) this;
    }

    /**
     * 获取单元格数据类型
     *
     * @return {@link DataType}
     */
    default DataType dataType() {
        if (cellIsNull()) {
            return null;
        }
        switch (getCell().getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(getCell())) {
                    return DataType.DATE;
                } else if (Optional.ofNullable(dataFormat()).orElse("").endsWith("%")) {
                    return DataType.PERCENT;
                } else {
                    return DataType.NUMBER;
                }
            case FORMULA:
                if (CellType.NUMERIC == getCell().getCachedFormulaResultType()) {
                    return DataType.NUMBER;
                }
                break;
            default:
                break;
        }
        return DataType.TEXT;
    }

    /**
     * 获取单元格数据类型，consumer里面参数是Optional，可以通过 .orElse() 指定默认值
     *
     * @param consumer {@link Consumer}{@link Consumer<Optional<DataType>>} 处理单元格数据类型
     * @return <T extends ICellReader>
     */
    default T dataType(final Consumer<Optional<DataType>> consumer) {
        consumer.accept(Optional.ofNullable(dataType()));
        return (T) this;
    }

    /**
     * 获取单元格数据类型
     *
     * @return {@link CellType}
     */
    default CellType cellType() {
        if (cellIsNull()) {
            return null;
        }
        return getCell().getCellType();
    }

    /**
     * 获取单元格数据类型，consumer里面参数是Optional，可以通过 .orElse() 指定默认值
     *
     * @param consumer {@link Consumer}{@link Consumer<Optional<CellType>>} 处理单元格数据类型
     * @return <T extends ICellReader>
     */
    default T cellType(final Consumer<Optional<CellType>> consumer) {
        consumer.accept(Optional.ofNullable(cellType()));
        return (T) this;
    }
}
