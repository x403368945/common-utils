package com.utils.excel;

import com.utils.excel.enums.DataType;
import com.utils.util.Dates;
import com.utils.util.Num;
import com.utils.util.Util;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Supplier;

import static com.utils.util.Dates.Pattern.yyyy_MM_dd;

/**
 * Cell单元格写入操作
 *
 * @author 谢长春 on 2018-8-8 .
 */
public interface ICellWriter<T extends ICellWriter> {
    /**
     * 获取当前操作单元格
     *
     * @return {@link Cell}
     */
    Cell getCell();

//    /**
//     * 选择操作单元格
//     *
//     * @param columnIndex int 列索引
//     * @return <T extends ISheet>
//     */
//    T cell(final int columnIndex);
//
//    /**
//     * 选择操作单元格
//     *
//     * @param column {@link Enum} 列名枚举定义
//     * @return HoldRow
//     */
//    default T cell(final Enum column) {
//        return cell(column.ordinal());
//    }

    /**
     * 获取克隆源样式
     *
     * @return {@link CloneStyles}
     */
    CloneStyles getCloneStyles();

    /**
     * 设置单元格为 CellType.BLANK 可以清空单元格内容，并保留样式
     *
     * @return <T extends ICellWriter>
     */
    default T setCellBlank() {
        getCell().setBlank();
        return (T) this;
    }

    /**
     * 设置单元格为 CellType.BLANK，跳过公式，公式列不清除；保留单元格样式
     *
     * @return <T extends ICellWriter>
     */
    default T setCellBlankIgnoreFormula() {
        if (!(CellType.FORMULA == getCell().getCellType() || CellType.FORMULA == getCell().getCachedFormulaResultType())) {
            getCell().setBlank();
        }
        return (T) this;
    }

    /**
     * 向当前单元格写入数据
     *
     * @param data {@link Cell} 数据单元格对象
     * @return <T extends ICellWriter>
     */
    default T write(final com.utils.excel.entity.Cell data) {
        writeStyle(data.getSindex()); // 必须先指定样式库（StylesTable），这里写入才有效
        if (Objects.isNull(data)) {
            setCellBlank();
            return (T) this;
        }
        if (Objects.nonNull(data.getFormula())) {
            writeFormula(data.getFormula());
            return (T) this; // 如果单元格有公式，则写完公式就跳出，所以需要提前return (T) this;
        }
        if (Objects.isNull(data.getType())) {
            data.setType(DataType.TEXT);
        }
        switch (data.getType()) {
            case DATE:
                if (Objects.nonNull(data.getValue()))
                    writeDate(data.date().date());
                break;
            case BIGDECIMAL:
                if (Objects.nonNull(data.getValue()))
                    writeNumber(data.number().bigDecimalValue());
                break;
            case LONG:
                if (Objects.nonNull(data.getValue()))
                    writeNumber(data.number().longValue());
                break;
            case INTEGER:
                if (Objects.nonNull(data.getValue()))
                    writeNumber(data.number().intValue());
                break;
            case SHORT:
                if (Objects.nonNull(data.getValue()))
                    writeNumber(data.number().shortValue());
                break;
            case NUMBER:
            case DOUBLE:
            case FLOAT:
            case PERCENT:
                if (Objects.nonNull(data.getValue()))
                    writeNumber(data.number().doubleValue());
                break;
            default:
                writeText(data.getText());
        }
        return (T) this;
    }

    /**
     * 自动识别当前写入单元格的数据类型，无法识别的都以字符串写入<br>
     * 尽量使用明确数据类型的方法写入，避免错误
     *
     * @param value Object 写入值
     * @return <T extends ICellWriter>
     */
    default T writeByCellType(final Object value) {
        if (Objects.isNull(value)) {
            return setCellBlank();
        }
//        if (Objects.isNull(getCell().getCellTypeEnum())) return writeText(Objects.toString(value));
        return write(getCell().getCellType(), value);
    }

    /**
     * 向当前单元格写入数据
     *
     * @param cellTypes {@link Map}{@link Map<Integer:columnIndex, CellType:写入数据单元格类型>}
     * @param value     Object 写入值
     * @return <T extends ICellWriter>
     */
    default T write(final Map<Integer, CellType> cellTypes, final Object value) {
        return write(cellTypes.getOrDefault(getCell().getColumnIndex(), CellType.STRING), value);
    }

    /**
     * 向当前单元格写入数据
     *
     * @param type  {@link CellType} 写入数据类型
     * @param value Object 写入值
     * @return <T extends ICellWriter>
     */
    default T write(final CellType type, final Object value) {
        if (Objects.isNull(value)) {
            setCellBlank();
        } else if (value instanceof Timestamp) {
            writeDate((Timestamp) value);
        } else if (value instanceof Date) {
            writeDate((Date) value);
        } else {
            switch (type) {
                case FORMULA:
                    writeFormula(Objects.toString(value, null));
                    break;
                case NUMERIC:
                    final String v = Objects.toString(value, "").trim();
                    if (v.matches("^[+-]?\\d+$")) {
                        writeNumber(Num.of(value).longValue());
                    } else if (v.matches("^[+-]?\\d+\\.\\d+$")) {
                        writeNumber(Num.of(value).doubleValue());
                    } else if (v.matches("^[+-]?\\d{4}-\\d{1,2}-\\d{1,2}$")) {
                        writeDate(Dates.of(Objects.toString(value), yyyy_MM_dd).timestamp());
                    } else {
                        writeText(v);
                    }
                    break;
                case BLANK:
                case STRING:
                case BOOLEAN: // Util.toBoolean(value)
                case ERROR:
                case _NONE:
                default:
                    writeText(Objects.toString(value, null));
            }
            }
        return (T) this;
    }

    /**
     * 向当前单元格写入数据
     *
     * @param type  {@link DataType} 写入数据类型
     * @param value Object 写入值
     * @return <T extends ICellWriter>
     */
    default T write(final DataType type, final Object value) {
        if (Objects.isNull(value)) {
            setCellBlank();
        } else if (value instanceof Timestamp) {
            writeDate((Timestamp) value);
        } else if (value instanceof Date) {
            writeDate((Date) value);
        } else {
            switch (type) {
                case SEQ:
                case NUMBER:
                case BIGDECIMAL:
                case DOUBLE:
                case FLOAT:
                case LONG:
                case INTEGER:
                case SHORT:
                case PERCENT:
                    writeNumber(Num.of(value).doubleValue());
                    break;
                case STRING:
                case TEXT:
                    writeText(Objects.toString(value, null));
                    break;
                case DATE:
                    writeDate(Dates.of(Objects.toString(value), yyyy_MM_dd).timestamp());
                    break;
                case FORMULA:
                    writeFormula(Objects.toString(value, null));
                    break;
            }
//            switch (type) {
//                case FORMULA:
//                    writeFormula(Objects.toString(value));
//                    break;
//                case NUMERIC:
//                    final String v = Objects.toString(value).trim();
//                    if (v.matches("^[+-]?\\d+$")) {
//                        writeNumber(Num.of(value).longValue());
//                    } else if (v.matches("^[+-]?\\d+\\.\\d+$")) {
//                        writeNumber(Num.of(value).doubleValue());
//                    } else if (v.matches("^[+-]?\\d{4}-\\d{1,2}-\\d{1,2}$")) {
//                        writeDate(Dates.of(Objects.toString(value), yyyy_MM_dd).timestamp());
//                    } else {
//                        writeText(v);
//                    }
//                    break;
//                case BLANK:
//                case STRING:
//                case BOOLEAN: // Util.toBoolean(value)
//                case ERROR:
//                case _NONE:
//                default:
//                    writeText(Objects.toString(value));
//            }
        }
        return (T) this;
    }

    /**
     * 向当前行单元格写入文本内容
     *
     * @param value String 文本内容
     * @return <T extends ICellWriter>
     */
    default T writeText(final String value) {
        if (Objects.isNull(value)) {
            setCellBlank();
        } else {
            getCell().setCellValue(value);
        }
        return (T) this;
    }

    /**
     * 向当前行单元格写入文本内容
     *
     * @param value String 文本内容
     * @return <T extends ICellWriter>
     */
    default T writeString(final String value) {
        return writeText(value);
    }

    /**
     * 向当前单元格写入数字
     *
     * @param value {@link Number} 数字
     * @return <T extends ICellWriter>
     */
    default T writeNumber(final Number value) {
        if (Objects.isNull(value)) {
            setCellBlank(); // 设置为 BLANK 可以清空单元格内容，保留样式
        } else {
//            getCell().setCellType(CellType.NUMERIC);
            getCell().setCellValue(value.doubleValue());
        }
        return (T) this;
    }

    /**
     * 向当前单元格写入日期，使用此方法必须通过 writeStyle 写入日期格式化样式
     *
     * @param value {@link Date} 日期
     * @return <T extends ICellWriter>
     */
    default T writeDate(final Date value) {
        if (Objects.isNull(value)) {
            setCellBlank(); // 设置为 BLANK 可以清空单元格内容，保留样式
        } else {
            getCell().setCellValue(value);
        }
        return (T) this;
    }

    /**
     * 向当前单元格写入日期，使用此方法必须通过 writeStyle 写入日期格式化样式
     *
     * @param value {@link Timestamp} 日期
     * @return <T extends ICellWriter>
     */
    default T writeDate(final Timestamp value) {
        if (Objects.isNull(value)) {
            setCellBlank(); // 设置为 BLANK 可以清空单元格内容，保留样式
        } else {
            getCell().setCellValue(value);
        }
        return (T) this;
    }

    /**
     * 向当前单元格写入日期
     *
     * @param value {@link String} 日期
     * @return <T extends ICellWriter>
     */
    default T writeDate(final String value) {
        if (Objects.isNull(value)) {
            setCellBlank(); // 设置为 BLANK 可以清空单元格内容，保留样式
        } else {
            getCell().setCellValue(value);
        }
        return (T) this;
    }

    /**
     * 向当前单元格写入公式
     *
     * @param formula {@link String} 公式
     * @return <T extends ICellWriter>
     */
    default T writeFormula(final String formula) {
        if (Util.isEmpty(formula)) {
            setCellBlank();
        } else {
            getCell().setCellFormula(formula);
        }
        return (T) this;
    }

    /**
     * 向当前单元格写入公式
     *
     * @param formula {@link Supplier}{@link Supplier<String:公式表达式>} 公式构造器
     * @return <T extends ICellWriter>
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
        if (Util.isEmpty(formula)) {
            setCellBlank();
        } else {
            getCell().setCellFormula(formula.get());
        }
        return (T) this;
    }

    /**
     * 向当前单元格写入批注<br>
     * 警告：批注框将会向后延伸两行两列，所以字数建议在30字左右，当内容过长时不会显示
     *
     * @param content {@link String} 批注内容
     * @return <T extends ICellWriter>
     */
    default T writeComment(final String content) {
        if (Objects.isNull(content)) {
            getCell().removeCellComment();
        } else {
            final Sheet sheet = getCell().getSheet();
            final CreationHelper factory = sheet.getWorkbook().getCreationHelper();
            final ClientAnchor anchor = factory.createClientAnchor();
            anchor.setRow1(getCell().getRowIndex());
            anchor.setCol1(getCell().getColumnIndex());
            anchor.setRow2(getCell().getRowIndex() + 2);
            anchor.setCol2(getCell().getColumnIndex() + 2);
            final Comment comment = sheet.createDrawingPatriarch().createCellComment(anchor);
            comment.setString(factory.createRichTextString(content));
        }
        return (T) this;
    }

    /**
     * 向当前单元格写入样式
     *
     * @param styleIndex int 样式索引，将会从样式库中获取样式
     * @return <T extends ICellWriter>
     */
    default T writeStyle(final int styleIndex) {
        writeStyle(getCloneStyles().clone(styleIndex));
        return (T) this;
    }

    /**
     * 向当前单元格写入样式
     *
     * @param cellStyle {@link CellStyle} 将要写入的样式
     * @return <T extends ICellWriter>
     */
    default T writeStyle(final CellStyle cellStyle) {
        if (Objects.nonNull(cellStyle)) {
            getCell().setCellStyle(cellStyle);
        }
        return (T) this;
    }

    /**
     * 向当前单元格写入下拉列表选项
     *
     * @param items {@link List}{@link List<String:下拉选项>}
     * @return <T extends ISheetWriter>
     */
    default T writeDropdownList(final List<String> items) {
        return writeDropdownList(items.toArray(new String[]{}));
    }

    /**
     * 向当前单元格写入下拉列表选项
     *
     * @param items {@link String[]:下拉选项}
     * @return <T extends ISheetWriter>
     */
    default T writeDropdownList(final String[] items) {
        final Sheet sheet = getCell().getSheet();
        final DataValidationHelper helper = sheet.getDataValidationHelper();
        final DataValidation validation = helper.createValidation(
//                 DVConstraint.createExplicitListConstraint(items),
                helper.createExplicitListConstraint(items),
                new CellRangeAddressList(getCell().getRowIndex(), getCell().getRowIndex(), getCell().getColumnIndex(), getCell().getColumnIndex())
        );
        validation.setSuppressDropDownArrow(true); // 控制下拉箭头是否显示：【true：显示，false：不显示】； 03 默认不显示下拉箭头，07 默认显示下拉箭头
        validation.setShowErrorBox(true); // 当输入了不合法的下拉选项，是否显示错误弹窗：【true：是，false：否】
        sheet.addValidationData(validation);
        return (T) this;
    }

    /**
     * 向当前单元格追加样式<br>
     * 以下代码可以创建单元格样式<br>
     * CellStyles.builder()fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.SkyBlue.color).build().createCellStyle(workbook);
     *
     * @param cellStyles {@link CellStyles} 将会追加的样式
     * @return <T extends ISheetWriter>
     */
    default T appendStyle(final CellStyles cellStyles) {
        Objects.requireNonNull(cellStyles, "参数【cellStyles】是必须的");
//       // 当索引为 0 ，表示未附加任何e样式，需要新建样式
//            if (0 == cell.getCellStyl().getIndex()) cell.setCellStyle(cellStyles.createCellStyle(workbook));
//            else cell.setCellStyle(cellStyles.appendClone(workbook, (CellStyle) cell.getCellStyle()));
        getCell().setCellStyle(cellStyles.appendClone(getCell().getSheet().getWorkbook(), getCell().getCellStyle()));
        return (T) this;
    }

    /**
     * 锁定当前单元格，禁止编辑<br>
     * 需要调用 {@link ISheetWriter#password(String)} 设置保护密码才生效<br>
     * 以下代码可以创建单元格样式<br>
     * CellStyles.builder().locked(true).fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.Grey25Percent.color).build().createCellStyle(workbook);
     *
     * @param cellStyles {@link CellStyles} 指定禁止编辑单元格样式
     * @return <T extends ISheetWriter>
     * @deprecated 锁定功能局限性比较大，建议事先设置好一个模板文件，将不可编辑的区域和样式设定好，然后加载该模板文件，直接向可编辑区域写数据即可；若需要向不可编辑区域写数据，可以通过密码加载模板文件，即可实现向不可编辑区域写数据
     */
    @Deprecated
    default T lock(final CellStyles cellStyles) {
        Objects.requireNonNull(cellStyles, "参数【cellStyles】是必须的");
        cellStyles.setLocked(true);
        appendStyle(cellStyles);
        return (T) this;
    }

    /**
     * 解锁当前单元格，取消禁止编辑
     * CellStyles.builder().locked(false).build().createCellStyle(workbook);
     *
     * @param cellStyles {@link CellStyles} 指定取消禁止编辑单元格样式
     * @return <T extends ISheetWriter>
     */
    default T unlock(final CellStyles cellStyles) {
        Objects.requireNonNull(cellStyles, "参数【cellStyles】是必须的");
        cellStyles.setLocked(false);
        appendStyle(cellStyles);
        return (T) this;
    }
}
