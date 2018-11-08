package com.utils.excel;

import com.utils.excel.entity.Position;
import com.utils.excel.entity.Range;
import com.utils.excel.enums.Column;
import com.utils.util.Num;
import lombok.*;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.formula.BaseFormulaEvaluator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.streaming.*;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;

import static com.utils.enums.Patterns.A_Z_FIND;
import static com.utils.enums.Patterns.d_FIND;

/**
 * Sheet 写操作相关的方法封装
 *
 * @author Jason Xie on 2018-8-8.
 */
public interface ISheetWriter<T extends ISheetWriter> extends ISheet<T>, ICellWriter<T> {
    @Builder
    class Options {
        /**
         * POI Excel 复制行规则，默认设置，会复制单元格值
         * 只有 XSSFWorkbook 支持行复制操作
         */
        @Builder.Default
        CellCopyPolicy cellCopyPolicy = new CellCopyPolicy().createBuilder().build();
        /**
         * 写入单元格依赖的样式库
         */
        @Builder.Default
        CloneStyles cloneStyles = new CloneStyles(null, null);
    }

    /**
     * POI Excel 复制行规则，默认设置，会复制单元格值
     *
     * @return {@link Options}
     */
    Options getOps();

    /**
     * 指定样式库
     *
     * @param stylesTable {@link StylesTable} 样式库来源
     * @return <T extends ISheetWriter>
     */
    default T setCloneStyles(final StylesTable stylesTable) {
        getOps().cloneStyles = new CloneStyles(stylesTable, getWorkbook());
        return (T) this;
    }

    /**
     * 指定样式库；从指定 {path}{name}.xlsx 文件读取样式库
     *
     * @param path Stirng 样式库文件绝对路径；只支持读取 .xlsx 后缀的样式
     * @return <T extends ISheetWriter>
     */
    default T setCloneStyles(final String path) {
        if (path.endsWith(".xlsx")) { // 非 .xlsx 后缀的文件直接跳过
            try {
                @Cleanup OPCPackage pkg = OPCPackage.open(path, PackageAccess.READ);
                setCloneStyles(new XSSFReader(pkg).getStylesTable());
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format("从【%s】文件读取样式异常", path), e);
            }
        }
        return (T) this;
    }

    /**
     * 获取克隆样式源
     *
     * @return {@link CloneStyles}
     */
    @Override
    default CloneStyles getCloneStyles() {
        return getOps().cloneStyles;
    }

    /**
     * 删除行，保留空行；单元格样式也会被清除
     *
     * @return <T extends ISheetWriter>
     */
    default T clearRow() {
        getSheet().removeRow(getRow());
        return (T) this;
    }

    /**
     * 删除行，整行上移
     *
     * @return <T extends ISheetWriter>
     */
    default T deleteRow() {
        final int rowIndex = getRowIndex();
        getSheet().shiftRows(rowIndex, rowIndex, 1);
        return (T) this;
    }

    /**
     * 选择操作行，当行不存在时创建新行
     *
     * @param rowIndex int 行索引
     * @return <T extends ISheetWriter>
     */
    default T rowOfNew(final int rowIndex) {
        return row(Optional.ofNullable(getSheet().getRow(rowIndex))
                .orElseGet(() -> getSheet().createRow(rowIndex))
        );
    }

    /**
     * 选择操作行，当行不存在时创建新行
     *
     * @param rownum {@link Rownum} 行索引
     * @return <T extends ISheetWriter>
     */
    default T rowOfNew(final Rownum rownum) {
        return rowOfNew(rownum.index());
    }

    /**
     * 新建操作行
     *
     * @param rowIndex int 行索引
     * @return <T extends ISheetWriter>
     */
    default T rowNew(final int rowIndex) {
        return row(getSheet().createRow(rowIndex));
    }

    /**
     * 新建操作行
     *
     * @param rownum {@link Rownum} 行索引
     * @return <T extends ISheetWriter>
     */
    default T rowNew(final Rownum rownum) {
        return rowNew(rownum.index());
    }

    /**
     * 选择下一行作为操作行，当行不存在时创建新行
     *
     * @return <T extends ISheetWriter>
     */
    default T nextRowOfNew() {
        return row(Optional.ofNullable(getSheet().getRow(getRowIndex() + 1))
                .orElseGet(() -> getSheet().createRow(getRowIndex() + 1))
        );
    }

    /**
     * 创建下一行实例作为操作行
     *
     * @return <T extends ISheetWriter>
     */
    default T nextRowNew() {
        return row(getSheet().createRow(getRowIndex() + 1));
    }

    /**
     * 清除当前行所有单元格内容，单元格样式保留
     *
     * @return <T extends ISheetWriter>
     */
    default T setRowBlank() {
        getRow().forEach(cell -> cell.setCellType(CellType.BLANK));
        return (T) this;
    }

    /**
     * 清除当前行所有单元格内容，单元格样式保留（跳过公式，单元格内容为公式时内容保留）
     *
     * @return <T extends ISheetWriter>
     */
    default T setRowBlankIgnoreFromula() {
        getRow().forEach(cell -> {
            switch (cell.getCellType()) {
                case BLANK:
                case FORMULA: // 公式单元格跳过，不执行清除操作
                    break;
                default:
//                    if (CellType.FORMULA != cell.getCachedFormulaResultTypeEnum())
                    cell.setCellType(CellType.BLANK);
            }
        });
        return (T) this;
    }

    /**
     * 选择操作单元格，当单元格不存在时创建单元格，并设置单元格类型为 CellType.BLANK
     *
     * @param columnIndex int 列索引
     * @return <T extends ISheetWriter>
     */
    default T cellOfNew(final int columnIndex) {
        return cell(Optional
                .ofNullable(getRow().getCell(columnIndex))
                .orElseGet(() -> getRow().createCell(columnIndex, CellType.BLANK))
        );
    }

    /**
     * 选择操作单元格，当单元格不存在时创建单元格，并设置单元格类型为 CellType.BLANK
     *
     * @param column {@link Column} 列名
     * @return <T extends ISheetWriter>
     */
    default T cellOfNew(final Column column) {
        return cellOfNew(column.ordinal());
    }


    /**
     * 新建操作单元格
     *
     * @param columnIndex int 列索引
     * @return <T extends ISheetWriter>
     */
    default T cellNew(final int columnIndex) {
        return cell(getRow().createCell(columnIndex, CellType.BLANK));
    }

    /**
     * 新建操作单元格
     *
     * @param column {@link Column} 列名
     * @return <T extends ISheetWriter>
     */
    default T cellNew(final Column column) {
        return cellOfNew(column.ordinal());
    }

    /**
     * 向当前行所有列追加样式
     *
     * @param cellStyles {@link CellStyles} 将此样式追加到所有列
     * @return <T extends ISheetWriter>
     */
    default T appendStyleOfRow(final CellStyles cellStyles) {
        Objects.requireNonNull(cellStyles, "参数【cellStyles】是必须的");
        for (int i = 0; i < getRow().getLastCellNum(); i++) {
            cellOfNew(i).appendStyle(cellStyles);
//            cellOfNew(i).getCell().setCellStyle(cellStyles.appendClone(getSheet().getWorkbook(), getCell().getCellStyle()));
        }
//        getRow().forEach(cell -> {
////            // 当索引为 0 ，表示未附加任何样式，需要新建样式
////            if (0 == cell.getCellStyle().getIndex()) cell.setCellStyle(cellStyles.createCellStyle(workbook));
////            else cell.setCellStyle(cellStyles.appendClone(workbook, (CellStyle) cell.getCellStyle()));
//            cell.setCellStyle(cellStyles.appendClone(getSheet().getWorkbook(), cell.getCellStyle()));
//        });
        return (T) this;
    }

    /**
     * 复制当前行到目标行
     *
     * @param toRowIndex int 目标行索引，非行号
     * @return <T extends ISheetWriter>
     */
    T copyTo(final int toRowIndex);
//
//    /**
//     * 此方法参考 org.apache.poi.xssf.usermodel.XSSFCell 类
//     * @param srcCell Cell 复制行
//     * @param destCell Cell 目标行
//     * @param policy CellCopyPolicy 复制规则
//     */
//    default void copyCellFrom(Cell srcCell, Cell destCell, CellCopyPolicy policy) {
//        if (policy.isCopyCellValue()) {
//            if (Objects.nonNull(srcCell)) {
//                CellType copyCellType = srcCell.getCellTypeEnum();
//                if (copyCellType == CellType.FORMULA && !policy.isCopyCellFormula()) copyCellType = srcCell.getCachedFormulaResultTypeEnum();
//                switch (copyCellType) {
//                    case NUMERIC:
//                        if (DateUtil.isCellDateFormatted(srcCell)) {
//                            destCell.setCellValue(srcCell.getDateCellValue());
//                        } else {
//                            destCell.setCellValue(srcCell.getNumericCellValue());
//                        }
//                        break;
//                    case STRING:
//                        destCell.setCellValue(srcCell.getStringCellValue());
//                        break;
//                    case FORMULA:
//                        destCell.setCellFormula(srcCell.getCellFormula());
//                        break;
//                    case BLANK:
////                        destCell.setBlank();
//                        break;
//                    case BOOLEAN:
//                        destCell.setCellValue(srcCell.getBooleanCellValue());
//                        break;
//                    case ERROR:
//                        destCell.setCellErrorValue(srcCell.getErrorCellValue());
//                        break;
//                    default:
//                        throw new IllegalArgumentException("Invalid cell type " + srcCell.getCellTypeEnum());
//                }
////            } else {
////                this.setBlank();
//            }
//        }
//        if (policy.isCopyCellStyle()) {
//            destCell.setCellStyle(srcCell == null ? null : srcCell.getCellStyle());
//        }
//        Hyperlink srcHyperlink = srcCell == null ? null : srcCell.getHyperlink();
//        if (policy.isMergeHyperlink()) {
//            if (srcHyperlink != null) {
//                destCell.setHyperlink(new XSSFHyperlink(srcHyperlink));
//            }
//        } else if (policy.isCopyHyperlink()) {
//            destCell.setHyperlink(srcHyperlink == null ? null : new XSSFHyperlink(srcHyperlink));
//        }
//    }

    /**
     * 复制当前行到下一行
     *
     * @return <T extends ISheetWriter>
     */
    default T copyToNext() {
        // row.getRowNum() 原生XSSFRow 拿到的 RowNum 实际上是行索引，不是行号
        copyTo(getRow().getRowNum() + 1);
        return (T) this;
    }

    /**
     * 合并单元格
     *
     * @param range {@link Range} 合并区域
     * @return <T extends ISheetWriter>
     */
    default T merge(final Range range) {
        getSheet().addMergedRegion(range.getCellRangeAddress());
        return (T) this;
    }

    /**
     * 冻结行和列<br>
     * freeze(1, 1) : 表示冻结第 1 列和第 1 行
     *
     * @param column int 冻结列号；为0表示不冻结或取消冻结
     * @param row    int 冻结行号；为0表示不冻结或取消冻结
     * @return <T extends ISheetWriter>
     */
    default T freeze(final int column, final int row) {
        getSheet().createFreezePane(column, row);
        return (T) this;
    }

    /**
     * 冻结行和列<br>
     * freeze(1, 1) : 表示冻结第 1 列和第 1 行
     *
     * @param column Column 冻结列号；为0表示不冻结或取消冻结
     * @param row    int 冻结行号；为0表示不冻结或取消冻结
     * @return <T extends ISheetWriter>
     */
    default T freeze(final Column column, final int row) {
        return freeze(column.ordinal() + 1, row);
    }

    /**
     * 冻结行和列<br>
     * freeze("A", 1) : 表示冻结第 1 列和第 1 行
     *
     * @param column String 冻结列名；null表示不冻结或取消冻结
     * @param row    int 冻结行号；为0表示不冻结或取消冻结
     * @return <T extends ISheetWriter>
     */
    default T freeze(final String column, final int row) {
        getSheet().createFreezePane(Objects.isNull(column) ? 0 : Position.ofColumn(column).columnIndex() + 1, row);
        return (T) this;
    }

    /**
     * 冻结行和列
     *
     * @param address {@link String} 冻结行列坐标； A2 表示冻结第2行和第1列；A 表示值冻结第 1 列；2 表示只冻结第 2 行
     * @return <T extends ISheetWriter>
     */
    default T freeze(final String address) {
        final Supplier<Integer> row = () -> {
            final Matcher m = d_FIND.matcher(address);
            // 冻结行列都是从 1 开始，所以这里需要 +1
            return m.find() ? Integer.parseInt(m.group()) + 1 : 0;
        };
        final Supplier<Integer> column = () -> {
            final Matcher m = A_Z_FIND.matcher(address);
            // 冻结行列都是从 1 开始，所以这里需要 +1
            return m.find() ? Position.ofColumn(m.group()).columnIndex() + 1 : 0;
        };
        getSheet().createFreezePane(column.get(), row.get());
        return (T) this;
    }


    /**
     * 向当前单元格写入公式；将会使用公式重构方案
     *
     * @param formula String 公式
     * @return <T extends ISheetWriter>
     */
    default T writeFormulaOfRebuild(final String formula) {
        writeFormula(() -> { // 获取公式
            if (formula.indexOf("{0}") > 0) { // 当公式使用 {0} 占位行号时，将 {0} 替换成行号
                return formula.replace("{0}", (getRow().getRowNum() + 1) + "");
            }
            // 重构规则说明：假设当前行号为100
            // 公式：A1+B1 > A100+B100
            // 公式：SUM(A1:C1) > SUM(A100:C100)
            // 公式：A1*C1 > A100*C100
            // 公式：A1*C1-D1 > A100*C100-D100
            // 公式(错误案例演示)：A1+A2+A3 > A100+A100+A100；因为：A1+A2+A3 属于跨行计算
            // 公式(错误案例演示)：SUM(A1:A3) > SUM(A100:A100)；因为：A1:A3 属于跨行计算
            // 以上案例说明，只支持横向的单行公式，不支持跨行和跨表
            return formula.replaceAll("(((?<=.?[A-Z])(\\d{1,10})(?=\\d?.*))|(?<=.?[A-Z])(\\d{1,10})$)", Objects.toString(getRow().getRowNum() + 1));
        });
        return (T) this;
    }

    /**
     * 向指定区域单元格写入下拉列表选项
     *
     * @param items List<String> 下拉选项集合
     * @param range {@link Range} 写入区域，A1:A10
     * @return <T extends ISheetWriter>
     */
    default T writeDropdownList(final List<String> items, final Range range) {
        return writeDropdownList(items.toArray(new String[]{}), range);
    }

    /**
     * 向指定区域单元格写入下拉列表选项<br>
     * 该方法写入的下拉选项有数量限制，可以将下拉选项写入到某个区域，使用该区域的值设置到下拉选项；参考：{@link ISheetWriter#writeDropdownList(String, Range)}
     *
     * @param items String[] 下拉选项集合；选项数量有限制，最大128
     * @param range {@link Range} 写入区域，A1:A10
     * @return <T extends ISheetWriter>
     */
    @SneakyThrows
    default T writeDropdownList(final String[] items, final Range range) {
        if (items.length > 128) {
            throw new Exception("下拉选项最大数量为128");
        }
        final DataValidationHelper helper = getSheet().getDataValidationHelper();
        final DataValidation validation = helper.createValidation(
//                 DVConstraint.createExplicitListConstraint(items),
                helper.createExplicitListConstraint(items),
                range.getCellRangeAddressList()
        );
        validation.setSuppressDropDownArrow(true); // 控制下拉箭头是否显示：【true：显示，false：不显示】； 03 默认不显示下拉箭头，07 默认显示下拉箭头
        validation.setShowErrorBox(true); // 当输入了不合法的下拉选项，是否显示错误弹窗：【true：是，false：否】
        getSheet().addValidationData(validation);
        return (T) this;
    }

    /**
     * 向指定区域单元格写入下拉列表选项，且下拉选项的值来至于指定 range 的区间
     *
     * @param ref   {@link String:sheetName!A1:B4} 下拉选项值引用区间表达式，必须指定引用区域的sheetName，如果引用的区域在当前Sheet，则可以使用{sheetName}占位；参考：【sheetName!A1:A5 | {sheetName}!A1:D1】
     * @param range {@link Range} 写入区域，A1:A10
     * @return <T extends ISheetWriter>
     */
    default T writeDropdownList(final String ref, final Range range) {
        final Name name = getWorkbook().createName(); // 创建引用名字
        name.setNameName(ref.replaceAll("[^A-Za-z0-9]", ""));
        name.setRefersToFormula(ref
                // 当使用 {sheetName} 占位时，使用当前 getSheet().getSheetName() 替换
                .replace("{sheetName}", getSheet().getSheetName())
                // 引用必须是绝对引用，所以这里将引用区间替换为绝对引用
                .replaceAll("([A-Z]+)(\\d+):([A-Z]+)(\\d+)", "\\$$1\\$$2:\\$$3\\$$4")
        );

        final DataValidationHelper helper = getSheet().getDataValidationHelper();
        final DataValidation validation = helper.createValidation(
                helper.createFormulaListConstraint(name.getNameName()),
                range.getCellRangeAddressList()
        );
        validation.setSuppressDropDownArrow(true); // 控制下拉箭头是否显示：【true：显示，false：不显示】； 03 默认不显示下拉箭头，07 默认显示下拉箭头
        validation.setShowErrorBox(true); // 当输入了不合法的下拉选项，是否显示错误弹窗：【true：是，false：否】
        getSheet().addValidationData(validation);

        return (T) this;
    }

    /**
     * 向指定区域单元格写入批注<br>
     * 警告：批注框将会向后延伸两行两列，所以字数建议在30字左右，当内容过长时不会显示
     *
     * @param content String 批注内容
     * @param range   {@link Range} 写入区域
     * @return <T extends ICellWriter>
     */
    default T writeComments(final String content, final Range range) {
        final Sheet sheet = getSheet();
        if (Objects.isNull(content)) { // 批注内容为空则删除该批注
            final CellRangeAddress rangeAddress = range.getCellRangeAddress();
            sheet.getCellComments().forEach((address, comment) -> {
                if (rangeAddress.isInRange(address.getRow(), address.getColumn())) // 批注在选定区间，则删除
                {
                    sheet.getRow(address.getRow()).getCell(address.getColumn()).removeCellComment();
                }
            });
        } else {
            final CreationHelper factory = sheet.getWorkbook().getCreationHelper();
            final ClientAnchor anchor = factory.createClientAnchor();
            range.forEach((rowIndex, columnIndex) -> {
                final Cell cell = Optional.ofNullable(sheet.getRow(rowIndex))
                        // 行存在，则判断列是否存在，列不存在则创建列
                        .map(row -> Optional.ofNullable(row.getCell(columnIndex)).orElseGet(() -> row.createCell(columnIndex)))
                        // 行不存在则创建行和列
                        .orElseGet(() -> sheet.createRow(rowIndex).createCell(columnIndex));
                if (Objects.isNull(cell.getCellComment())) { // 批注不存在
                    anchor.setRow1(rowIndex);
                    anchor.setCol1(columnIndex);
                    anchor.setRow2(rowIndex + 2);
                    anchor.setCol2(columnIndex + 2);
                    final Comment comment = sheet.createDrawingPatriarch().createCellComment(anchor);
                    comment.setString(factory.createRichTextString(content));
                } else { // 批注存在则替换内容
                    cell.getCellComment().setString(factory.createRichTextString(content));
                }
            });
        }
        return (T) this;
    }
//    /**
//     * 向当前单元格写入批注
//     *
//     * @param title String {@link String} 批注标题
//     * @param content String {@link String} 批注内容
//     * @param start Position {@link Position} 起始单元格
//     * @param end   Position {@link Position} 截止单元格
//     * @return <T extends ISheetWriter>
//     */
//    default T writeComment(final String title, final String content, final Position start, final Position end) {
//        final DataValidationHelper helper = getSheet().getDataValidationHelper();
//        final DataValidation validation = helper.createValidation(
////                        DVConstraint.createCustomFormulaConstraint("BB1");
//                helper.createCustomConstraint("A2"),
//                new CellRangeAddressList(start.index(), end.index(), start.columnIndex(), end.columnIndex())
//        );
//        validation.createPromptBox(title, content);
//        getSheet().addValidationData(validation);
//
////        // 构造constraint对象
////        DVConstraint constraint = DVConstraint.createCustomFormulaConstraint("BB1");
////        // 四个参数分别是：起始行、终止行、起始列、终止列
////        CellRangeAddressList regions = new CellRangeAddressList(firstRow,endRow,firstCol, endCol);
////        // 数据有效性对象
////        HSSFDataValidation data_validation_view = new HSSFDataValidation(regions,constraint);
////        data_validation_view.createPromptBox(promptTitle, promptContent);
////        sheet.addValidationData(data_validation_view);
//
//        return (T) this;
//    }

//    /**
//     * 强制刷新公式，自动调整列宽
//     *
//     * @return <T extends ISheetWriter>
//     */
//    default T flush() {
//        XSSFFormulaEvaluator.evaluateAllFormulaCells(getWorkbook());
//        Sheet sheet = getSheet();
//        for (Cell cell : sheet.getRow(0)) {
//            sheet.autoSizeColumn(cell.getColumnIndex());
//        }
//        return (T) this;
//    }

    /**
     * 设置行分组
     *
     * @param fromRowIndex int 起始行索引，包含
     * @param toRowIndex   int 结束行索引，包含
     * @return <T extends ISheetWriter>
     */
    default T groupRow(final int fromRowIndex, final int toRowIndex) {
        getSheet().groupRow(fromRowIndex, toRowIndex);
        return (T) this;
    }

    /**
     * 设置行分组
     *
     * @param range {@link com.utils.util.Num.RangeInt} 分组行索引区间
     * @return <T extends ISheetWriter>
     */
    default T groupRow(final Num.RangeInt range) {
        groupRow(range.getMin(), range.getMax());
        return (T) this;
    }

    /**
     * 设置列分组
     *
     * @param fromColumnIndex int 起始列索引，包含
     * @param toColumnIndex   int 结束列索引，包含
     * @return <T extends ISheetWriter>
     */
    default T groupColumn(final int fromColumnIndex, final int toColumnIndex) {
        getSheet().groupColumn(fromColumnIndex, toColumnIndex);
        return (T) this;
    }

    /**
     * 设置列分组
     *
     * @param fromColumn {@link Column}  起始列，包含
     * @param toColumn   {@link Column}  结束列，包含
     * @return <T extends ISheetWriter>
     */
    default T groupColumn(final Column fromColumn, final Column toColumn) {
        return groupColumn(fromColumn.ordinal(), toColumn.ordinal());
    }

    /**
     * 设置列分组
     *
     * @param range {@link com.utils.util.Num.RangeInt} 分组列索引区间
     * @return <T extends ISheetWriter>
     */
    default T groupColumn(final Num.RangeInt range) {
        groupColumn(range.getMin(), range.getMax());
        return (T) this;
    }


    /**
     * 隐藏行
     *
     * @param rowIndex {@link int} 隐藏行索引
     * @return <T extends ISheetWriter>
     */
    default T hideRow(final int rowIndex) {
        row(rowIndex).getRow().setZeroHeight(true);
        return (T) this;
    }

    /**
     * 隐藏列
     *
     * @param columnIndex {@link int} 隐藏列索引
     * @return <T extends ISheetWriter>
     */
    default T hideColumn(final int columnIndex) {
        getSheet().setColumnHidden(columnIndex, true);
        return (T) this;
    }

    /**
     * 隐藏列
     *
     * @param column {@link Column} 隐藏列
     * @return <T extends ISheetWriter>
     */
    default T hideColumn(final Column column) {
        return hideColumn(column.ordinal());
    }

    /**
     * 自动调整列宽；该方法效果不太理想
     *
     * @return <T extends ISheetWriter>
     */
    default T autoColumnWidth() {
//        汉字是512，数字是256.
//        setColumnWidth(i,value.toString().length() * 512);
        final Sheet sheet = getSheet();
        for (Cell cell : sheet.getRow(0)) {
            sheet.autoSizeColumn(cell.getColumnIndex(), true);
        }
        return (T) this;
    }

    /**
     * 刷新公式单元格
     *
     * @return <T extends ISheetWriter>
     */
    default T evaluateAllFormulaCells() {
        if (getWorkbook() instanceof HSSFWorkbook) {
            HSSFFormulaEvaluator.evaluateAllFormulaCells((HSSFWorkbook) getWorkbook());
        } else if (getWorkbook() instanceof SXSSFWorkbook) {
            SXSSFFormulaEvaluator.evaluateAllFormulaCells((SXSSFWorkbook) getWorkbook(), false);
        } else //if (getWorkbook() instanceof XSSFWorkbook)
        {
            BaseFormulaEvaluator.evaluateAllFormulaCells(getWorkbook());
        }
        return (T) this;
    }

    /**
     * 锁定当前选定区域单元格，禁止编辑<br>
     * 需要调用 {@link ISheetWriter#password(String)} 设置保护密码才生效<br>
     * 以下代码可以创建单元格样式<br>
     * CellStyles.builder().locked(true).fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.Grey25Percent.color).build().createCellStyle(workbook);
     *
     * @param range      {@link Range} 锁定区间内的所有单元格
     * @param cellStyles {@link CellStyles} 指定禁止编辑单元格样式
     * @return <T extends ISheetWriter>
     * @deprecated 锁定功能局限性比较大，建议事先设置好一个模板文件，将不可编辑的区域和样式设定好，然后加载该模板文件，直接向可编辑区域写数据即可；若需要向不可编辑区域写数据，可以通过密码加载模板文件，即可实现向不可编辑区域写数据
     */
    @Deprecated
    default T lock(final Range range, final CellStyles cellStyles) {
        Objects.requireNonNull(range, "参数【range】是必须的");
        Objects.requireNonNull(cellStyles, "参数【cellStyles】是必须的");
        cellStyles.setLocked(true);
        range.forEach((rowIndex, columnIndex) ->
                rowOfNew(rowIndex).cellOfNew(columnIndex).appendStyle(cellStyles)
        );
        return (T) this;
    }

    /**
     * 解锁当前选定区域单元格，取消禁止编辑
     * CellStyles.builder().locked(false).build().createCellStyle(workbook);
     *
     * @param range      {@link Range} 锁定区间内的所有单元格
     * @param cellStyles {@link CellStyles} 指定取消禁止编辑单元格样式
     * @return <T extends ISheetWriter>
     */
    default T unlock(final Range range, final CellStyles cellStyles) {
        Objects.requireNonNull(range, "参数【range】是必须的");
        Objects.requireNonNull(cellStyles, "参数【cellStyles】是必须的");
        cellStyles.setLocked(false);
        range.forEach((rowIndex, columnIndex) ->
                rowOfNew(rowIndex).cellOfNew(columnIndex).appendStyle(cellStyles)
        );
        return (T) this;
    }

    /**
     * 设置密码之后将会全表禁止编辑，除非设置 {@link ICellWriter#unlock(CellStyles)} 的单元格才允许编辑
     * 锁定的单元格不设置密码，依然可以编辑；03版excel不支持该操作
     *
     * @param password {@link String} 锁定的单元格保护密码
     * @return <T extends ISheetWriter>
     */
    @SneakyThrows
    default T password(final String password) {
        Objects.requireNonNull((Objects.isNull(password) || password.length() == 0) ? null : true, "参数【password】是必须的");
        getSheet().protectSheet(password);
        if (getSheet() instanceof HSSFSheet) {
            // 不支持 enableLocking
        } else if (getSheet() instanceof XSSFSheet) {
            final XSSFSheet sheet = ((XSSFSheet) getSheet());
            sheet.enableLocking();
            sheet.lockSelectLockedCells(true); // true：禁止选择锁定的单元格
            sheet.lockSelectUnlockedCells(false); // true：禁止选择未锁定的单元格
            sheet.lockFormatCells(false); // true：禁止调整单元格样式
            sheet.lockFormatColumns(false); // true：禁止调整列样式
            sheet.lockFormatRows(false); // true：禁止调整行样式
            sheet.lockInsertColumns(false); // true：禁止插入列
            sheet.lockInsertRows(false); // true：禁止插入行
            sheet.lockInsertHyperlinks(true); // true：禁止插入超链接
            sheet.lockDeleteColumns(false); // true：禁止删除列
            sheet.lockDeleteRows(false); // true：禁止删除行
            sheet.lockSort(false); // true：禁止排序
            sheet.lockAutoFilter(true);
            sheet.lockPivotTables(true);
            sheet.lockObjects(true);
            sheet.lockScenarios(true);
        } else if (getSheet() instanceof SXSSFSheet) {
            final SXSSFSheet sheet = ((SXSSFSheet) getSheet());
            sheet.enableLocking();
            sheet.lockSelectLockedCells(true); // true：禁止选择锁定的单元格
            sheet.lockSelectUnlockedCells(false); // true：禁止选择未锁定的单元格
            sheet.lockFormatCells(false); // true：禁止调整单元格样式
            sheet.lockFormatColumns(false); // true：禁止调整列样式
            sheet.lockFormatRows(false); // true：禁止调整行样式
            sheet.lockInsertColumns(false); // true：禁止插入列
            sheet.lockInsertRows(false); // true：禁止插入行
            sheet.lockInsertHyperlinks(true); // true：禁止插入超链接
            sheet.lockDeleteColumns(false); // true：禁止删除列
            sheet.lockDeleteRows(false); // true：禁止删除行
            sheet.lockSort(false); // true：禁止排序
            sheet.lockAutoFilter(true);
            sheet.lockPivotTables(true);
            sheet.lockObjects(true);
            sheet.lockScenarios(true);
        }
        return (T) this;
    }

    /**
     * 关闭 Workbook 对象
     */
    @SneakyThrows
    @Override
    default void close() {
        getWorkbook().close();
    }

    /**
     * 不同版本的 excel 执行复制操作相关的方法封装IS
     *
     * @param <T>
     */
    interface ICopyRows<T> {
        CellCopyPolicy DEFAULT_CELL_COPY_POLICY = new CellCopyPolicy().createBuilder().build();

        interface ICopy {
            /**
             * 复制行参数
             *
             * @param sheet             {@link Sheet}
             * @param fromStratRowIndex int 起始区间，行索引
             * @param fromEndRowIndex   int 起始区间，行索引
             * @param toRowIndex        int 目标行索引
             * @param repeatCount       int 重复次数
             * @param cellCopyPolicy    {@link CellCopyPolicy} 行复制规则
             */
            void copy(final Sheet sheet,
                      final int fromStratRowIndex,
                      final int fromEndRowIndex,
                      final int toRowIndex,
                      final int repeatCount,
                      CellCopyPolicy cellCopyPolicy);
        }

        enum SheetTypes {
            /**
             * .xlsx 写入
             */
            XSSFSHEET(".xlsx",
                    (sheet) -> sheet instanceof XSSFSheet,
                    (sheet, fromStratRowIndex, fromEndRowIndex, toRowIndex, repeatCount, cellCopyPolicy) -> {
                        // 实现 .xlsx 行复制功能
                        // Sheet sheet, int fromStratRowIndex, int fromEndRowIndex, int toRowIndex, int repeatCount, CellCopyPolicy cellCopyPolicy
                        if (Objects.isNull(cellCopyPolicy)) {
                            cellCopyPolicy = DEFAULT_CELL_COPY_POLICY;
                        }
                        final XSSFSheet xsheet = (XSSFSheet) sheet;
                        for (int i = 0; i < repeatCount; i++) {
                            xsheet.copyRows(fromStratRowIndex, fromEndRowIndex, toRowIndex + i + (i * (fromEndRowIndex - fromStratRowIndex)), cellCopyPolicy);
                        }
                    }),
            /**
             * .xlsx限制最大缓存写入
             */
            SXSSFSHEET(".xlsx限制最大缓存写入",
                    (sheet) -> sheet instanceof SXSSFSheet,
                    (sheet, fromStratRowIndex, fromEndRowIndex, toRowIndex, repeatCount, cellCopyPolicy) -> {
                        // 实现 .xlsx 带最大缓存航的 行复制功能
                        // 本身并不支持复制操作，尝试实现复制操作
//                        Stream.iterate(0, v -> v + 1).limit(repeatCount).forEach(i -> { });
                        final CellCopyPolicy policy = Objects.isNull(cellCopyPolicy) ? DEFAULT_CELL_COPY_POLICY : cellCopyPolicy;
                        for (int i = 0; i < repeatCount; i++) {
                            for (int j = fromStratRowIndex; j <= fromEndRowIndex; j++) {
                                // 参考:org.apache.poi.xssf.usermodel.XSSFRow#copyRowFrom
                                // 自定义实现 SXSSFSheet 复制行操作
                                // 只支持单行复制，且公式列只支持号替换行
                                final SXSSFRow srcRow = ((SXSSFSheet) sheet).getRow(fromStratRowIndex);
                                final SXSSFRow destRow = ((SXSSFSheet) sheet).createRow(toRowIndex + i + (i * (fromEndRowIndex - fromStratRowIndex)));
                                srcRow.forEach(srcCell -> { // 循环复制单元格
                                    final SXSSFCell destCell = destRow.createCell(srcCell.getColumnIndex(), srcCell.getCellType());
                                    { // 参考: org.apache.poi.xssf.usermodel.XSSFCell > copyCellFrom
                                        if (policy.isCopyCellValue()) {
                                            CellType copyCellType = srcCell.getCellType();
                                            if (copyCellType == CellType.FORMULA && !policy.isCopyCellFormula()) {
                                                copyCellType = srcCell.getCachedFormulaResultType();
                                            }

                                            switch (copyCellType) {
                                                case NUMERIC:
                                                    if (DateUtil.isCellDateFormatted(srcCell)) {
                                                        destCell.setCellValue(srcCell.getDateCellValue());
                                                    } else {
                                                        destCell.setCellValue(srcCell.getNumericCellValue());
                                                    }
                                                    break;
                                                case STRING:
                                                    destCell.setCellValue(srcCell.getStringCellValue());
                                                    break;
                                                case FORMULA:
                                                    // 重写公式规则说明：假设当前行号为100
                                                    // 公式：A1+B1 > A100+B100
                                                    // 公式：SUM(A1:C1) > SUM(A100:C100)
                                                    // 公式：A1*C1 > A100*C100
                                                    // 公式：A1*C1-D1 > A100*C100-D100
                                                    // 公式(无法处理案例演示)：A1+A2+A3 > A100+A2+A3；因为：A1+A2+A3 属于跨行计算
                                                    // 公式(无法处理案例演示)：SUM(A1:A3) > SUM(A100:A3)；因为：A1:A3 属于跨行计算
                                                    // 以上案例说明，只支持横向的单行公式，不支持跨行和跨表
                                                    destCell.setCellFormula(
                                                            srcCell.getCellFormula()
                                                                    .replaceAll(
                                                                            String.format("(((?<=.?[A-Z])(%d)(?=\\D?.*))|(?<=.?[A-Z])(%d)$)", srcRow.getRowNum() + 1, srcRow.getRowNum() + 1),
                                                                            (destRow.getRowNum() + 1) + ""
                                                                    )
                                                    );
                                                    break;
                                                case BLANK:
                                                    destCell.setCellType(CellType.BLANK);
                                                    break;
                                                case BOOLEAN:
                                                    destCell.setCellValue(srcCell.getBooleanCellValue());
                                                    break;
                                                case ERROR:
                                                    destCell.setCellErrorValue(srcCell.getErrorCellValue());
                                                    break;
                                                default:
                                                    throw new IllegalArgumentException("Invalid cell type " + srcCell.getCellType());
                                            }
                                        }
                                        if (policy.isCopyCellStyle()) {
                                            destCell.setCellStyle(srcCell.getCellStyle());
                                        }
                                        Hyperlink srcHyperlink = srcCell.getHyperlink();
                                        if (policy.isMergeHyperlink()) {
                                            if (Objects.nonNull(srcHyperlink)) {
                                                destCell.setHyperlink(new XSSFHyperlink(srcHyperlink));
                                            }
                                        } else if (policy.isCopyHyperlink()) {
                                            if (Objects.nonNull(srcHyperlink)) {
                                                destCell.setHyperlink(new XSSFHyperlink(srcHyperlink));
                                            }
                                        }
                                    }
                                });
                                if (policy.isCopyRowHeight()) {
                                    destRow.setHeight(srcRow.getHeight());
                                }
                            }
                        }
                        // 设置合并单元格
                        // 参考： org.apache.poi.xssf.usermodel.XSSFRow#copyRowFrom
                        if (policy.isCopyMergedRegions()) {
                            sheet.getMergedRegions().forEach(cellRangeAddress -> {
                                if (fromStratRowIndex == cellRangeAddress.getFirstRow()) { // fromStratRowIndex == cellRangeAddress.getLastRow()
                                    for (int i = 0; i < repeatCount; i++) {
                                        final CellRangeAddress destRegion = cellRangeAddress.copy();
                                        final int offset = (toRowIndex + i + (i * (fromEndRowIndex - fromStratRowIndex))) - destRegion.getFirstRow();
                                        destRegion.setFirstRow(destRegion.getFirstRow() + offset);
                                        destRegion.setLastRow(destRegion.getLastRow() + offset);
                                        sheet.addMergedRegion(destRegion);
                                    }
                                }
                            });
                        }
                    }),
            /**
             * .xls 写入
             */
            HSSFSHEET(".xls",
                    (sheet) -> sheet instanceof HSSFSheet,
                    (sheet, fromStratRowIndex, fromEndRowIndex, toRowIndex, repeatCount, cellCopyPolicy) -> {
                        // 实现 .xls 行复制功能

                    }),
            ;
            final String comment;
            final Predicate<Sheet> match;
            final ICopy instance;

            SheetTypes(final String comment, final Predicate<Sheet> match, final ICopy instance) {
                this.comment = comment;
                this.match = match;
                this.instance = instance;
            }
        }

        /**
         * 获取当前操作Sheet
         *
         * @return {@link Sheet}
         */
        Sheet getSheet();

        /**
         * 复制指定行到目标行
         *
         * @param fromRowIndex int 被复制行索引，非行号
         * @param toRowIndex   int 目标行索引，非行号
         * @return <T extends ISheetWriter>
         */
        default T copy(final int fromRowIndex, final int toRowIndex) {
            SheetTypes.valueOf(getSheet().getClass().getSimpleName().toUpperCase())
                    .instance
                    .copy(getSheet(), fromRowIndex, fromRowIndex, toRowIndex, 1, null);
//            if(SheetTypes.XSSFSHEET.match.test(getSheet()))
//                SheetTypes.XSSFSHEET.instance.copy(getSheet(), fromRowIndex, fromRowIndex, toRowIndex, 1, null);
//            else if(SheetTypes.HSSFSHEET.match.test(getSheet()))
//                SheetTypes.HSSFSHEET.instance.copy(getSheet(), fromRowIndex, fromRowIndex, toRowIndex, 1, null);
//            else if(SheetTypes.SXSSFSHEET.match.test(getSheet()))
//                SheetTypes.SXSSFSHEET.instance.copy(getSheet(), fromRowIndex, fromRowIndex, toRowIndex, 1, null);

            return (T) this;
        }

        /**
         * 复制指定行到目标行，目标行可以是多行，通过count指定目标行数
         *
         * @param fromRowIndex int 被复制行索引，非行号
         * @param toRowIndex   int 目标行索引，非行号
         * @param repeatCount  int 总共复制多少行
         * @return <T extends ISheetWriter>
         */
        default T copy(final int fromRowIndex, int toRowIndex, final int repeatCount) {
            SheetTypes.valueOf(getSheet().getClass().getSimpleName().toUpperCase())
                    .instance
                    .copy(getSheet(), fromRowIndex, fromRowIndex, toRowIndex, repeatCount, null);

            return (T) this;
        }

        /**
         * 复制指定行到目标行，目标行可以是多行，通过count指定目标行数
         *
         * @param fromRowIndex   int 被复制行索引，非行号
         * @param toRowIndex     int 目标行索引，非行号
         * @param repeatCount    int 总共复制多少行
         * @param cellCopyPolicy {@link CellCopyPolicy} POI Excel 复制行规则；只有 XSSFWorkbook 支持行复制操作
         * @return <T extends ISheetWriter>
         */
        default T copy(final int fromRowIndex, int toRowIndex, final int repeatCount, CellCopyPolicy cellCopyPolicy) {
            SheetTypes.valueOf(getSheet().getClass().getSimpleName().toUpperCase())
                    .instance
                    .copy(getSheet(), fromRowIndex, fromRowIndex, toRowIndex, repeatCount, cellCopyPolicy);
            return (T) this;
        }

        /**
         * 复制指定行到目标行
         *
         * @param row        {@link Row} 指定行
         * @param toRowIndex int 目标行索引，非行号
         * @return <T extends ISheetWriter>
         */
        default T copyTo(final Row row, final int toRowIndex) {
            SheetTypes.valueOf(getSheet().getClass().getSimpleName().toUpperCase())
                    .instance
                    .copy(getSheet(), row.getRowNum(), row.getRowNum(), toRowIndex, 1, null);
            return (T) this;
        }
    }

    /**
     * 获得行保持状态
     *
     * @return {@link HoldRow}
     */
    default HoldRow holdRow() {
        return HoldRow.of(getRow());
    }

    /**
     * 行状态保持对象，小计行部分操作需要在子节点写入完成之后，所以需要保持行
     */
    @RequiredArgsConstructor(staticName = "of")
    class HoldRow implements ICellWriter<HoldRow> {

        @NonNull
        private final Row row;
        private Cell cell;

        @Override
        public Cell getCell() {
            return cell;
        }

        @Override
        public CloneStyles getCloneStyles() {
            return null;
        }

        /**
         * 选择操作单元格
         *
         * @param columnIndex int 列索引
         * @return HoldRow
         */
        public HoldRow cell(final int columnIndex) {
            cell = Objects.isNull(row) ? null : row.getCell(columnIndex);
            return this;
        }

        /**
         * 选择操作单元格
         *
         * @param column {@link Column} 列名枚举定义
         * @return HoldRow
         */
        public HoldRow cell(final Column column) {
            cell(column.ordinal());
            return this;
        }

        /**
         * 选择操作单元格，当单元格不存在时创建单元格，并设置单元格类型为 CellType.BLANK
         *
         * @param columnIndex int 列索引
         * @return HoldRow
         */
        public HoldRow cellOfNew(final int columnIndex) {
            cell = Optional
                    .ofNullable(row.getCell(columnIndex))
                    .orElseGet(() -> row.createCell(columnIndex, CellType.BLANK));
            return this;
        }

        /**
         * 新建操作单元格
         *
         * @param columnIndex int 列索引
         * @return HoldRow
         */
        public HoldRow cellNew(final int columnIndex) {
            cell = row.createCell(columnIndex, CellType.BLANK);
            return this;
        }

        /**
         * 当前操作对象作为参数
         *
         * @param consumer Consumer<HoldRow>
         */
        public void end(final Consumer<HoldRow> consumer) {
            consumer.accept(this);
        }

    }

//    interface ICopy {
//        class Options {
//            private int fromStratRowIndex;
//            private int fromEndRowIndex;
//            private int toRowIndex;
//            private int repeatCount;
//            private CellCopyPolicy cellCopyPolicy;
//        }
//
//        /**
//         * 复制行操作，只有 XSSFWorkbook 支持行复制操作，其他的需自己实现
//         *
//         * @param sheet             Sheet 当前操作Sheet
//         * @param fromStratRowIndex int 开始行索引
//         * @param fromEndRowIndex   int 结束行索引
//         * @param toRowIndex        int 目标开始行索引
//         * @param repeatCount       int 重复次数
//         * @param cellCopyPolicy    CellCopyPolicy POI Excel 复制行规则；
//         */
//        
//
////        /**
////         * 复制指定行到目标行
////         *
////         * @param fromRowIndex int 被复制行索引，非行号
////         * @param toRowIndex   int 目标行索引，非行号
////         */
////        default void copy(final int fromRowIndex, final int toRowIndex) {
////            copy(fromRowIndex, toRowIndex, 1);
////        }
////
////        /**
////         * 复制指定行到目标行，目标行可以是多行，通过count指定目标行数
////         *
////         * @param fromRowIndex int 被复制行索引，非行号
////         * @param toRowIndex   int 目标行索引，非行号
////         * @param count        int 总共复制多少行
////         */
////        default void copy(final int fromRowIndex, int toRowIndex, final int count) {
////            copy(fromRowIndex, toRowIndex, count, null);
////        }
////
////        /**
////         * 复制指定行到目标行，目标行可以是多行，通过count指定目标行数
////         *
////         * @param fromRowIndex   int 被复制行索引，非行号
////         * @param toRowIndex     int 目标行索引，非行号
////         * @param count          int 总共复制多少行
////         * @param cellCopyPolicy CellCopyPolicy POI Excel 复制行规则；只有 XSSFWorkbook 支持行复制操作
////         */
////        default void copy(final int fromRowIndex, int toRowIndex, final int count, CellCopyPolicy cellCopyPolicy) {
////            if (Objects.isNull(cellCopyPolicy))
////                cellCopyPolicy = new CellCopyPolicy().createBuilder().build(); // 默认复制行规则
////        }
////
////        /**
////         * 复制指定行到目标行
////         *
////         * @param row        Row 指定行
////         * @param toRowIndex int 目标行索引，非行号
////         */
////        default void copyTo(final Row row, final int toRowIndex) {
////        }
////
////        /**
////         * xls 后缀的文件复制行操作；一般作用为 ICopy 提供适配实现，也可以单独使用
////         */
////        class HSSFSheetCopy implements ICopy {
////
////            @Override
////            public void copyTo(Row row, int toRowIndex) {
////                throw new RuntimeException("暂不支持该操作");
////            }
////        }
////
////        /**
////         * xlsx 后缀的文件复制行操作；一般作用为 ICopy 提供适配实现，也可以单独使用
////         */
////        class XSSFSheetCopy implements ICopy {
//////            @Builder
//////            @NoArgsConstructor
//////            @AllArgsConstructor
//////            class Options {
//////                private XSSFSheet sheet;
//////                private XSSFRow fromRow;
//////                private int fromRowIndex;
//////                private int toRowIndex;
//////            }
////
////            @Override
////            public void copyTo(XSSFSheet sheet, XSSFRow row, int toRowIndex) {
////            }
////        }
////
////        /**
////         * xlsx 后缀且带最大缓存行的文件复制行操作；一般作用为 ICopy 提供适配实现，也可以单独使用
////         */
////        class SXSSFSheetCopy implements ICopy {
////
////            @Override
////            public void copyTo(Row row, int toRowIndex) {
////                throw new RuntimeException("暂不支持该操作");
////            }
////        }
//    }

}