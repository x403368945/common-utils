package com.utils.util.excel;

import com.utils.common.entity.excel.Position;
import com.utils.common.entity.excel.Rownum;
import com.utils.enums.DataType;
import com.utils.util.Util;
import lombok.Builder;
import lombok.Cleanup;
import lombok.NonNull;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * excel > sheet 写入操作接口
 *
 * @author Jason Xie on 2017/11/7.
 */
public interface ISheetWriter {
    @Builder
    class Options {
        /**
         * POI Excel 复制行规则，默认设置，会复制单元格值
         * 只有 XSSFWorkbook 支付行复制操作
         */
        @Builder.Default
        private CellCopyPolicy cellCopyPolicy = new CellCopyPolicy().createBuilder().build();
        /**
         * 写入单元格依赖的样式库
         */
        @Builder.Default
        private CloneStyles cloneStyles = new CloneStyles(null, null);
        /**
         * 是否对公式执行 rebuild 操作
         */
        @Builder.Default
        private boolean rebuildFormula = false;
    }

    /**
     * POI Excel 复制行规则，默认设置，会复制单元格值
     */
    Options getOps();

    Workbook getWorkbook();

    Sheet getSheet();

    /**
     * 当前操作行
     */
    Row getRow();

    /**
     * 当前操作列
     */
    Cell getCell();

    /**
     * 指定样式库
     *
     * @param stylesTable StylesTable 样式库
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T setCloneStyles(final StylesTable stylesTable) {
        getOps().cloneStyles = new CloneStyles(stylesTable, getWorkbook());
        return (T) this;
    }

    /**
     * 指定样式库；从指定 {path}{name}.xlsx 文件读取样式库
     *
     * @param path Stirng 样式库文件绝对路径；只支持读取 .xlsx 后缀的样式
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T setCloneStyles(final String path) {
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
     * 复制指定行到目标行
     *
     * @param fromRowIndex int 被复制行索引，非行号
     * @param toRowIndex   int 目标行索引，非行号
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T copy(final int fromRowIndex, final int toRowIndex) {
        return copy(fromRowIndex, toRowIndex, 1);
    }

    /**
     * 复制指定行到目标行，目标行可以是多行，通过count指定目标行数
     *
     * @param fromRowIndex int 被复制行索引，非行号
     * @param toRowIndex   int 目标行索引，非行号
     * @param count        int 总共复制多少行
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T copy(final int fromRowIndex, int toRowIndex, final int count) {
        if (getSheet() instanceof XSSFSheet) {
            for (int i = 0; i < count; i++) {
                ((XSSFSheet) getSheet()).copyRows(fromRowIndex, fromRowIndex, toRowIndex++, getOps().cellCopyPolicy);
            }
        } else {
            row(fromRowIndex);
            for (int i = 0; i < count; i++) {
                this.copyTo(toRowIndex++);
            }
//            throw new RuntimeException(getSheet().getClass().toString()+" 不支持复制操作");
        }
        return (T) this;
    }

    /**
     * 复制当前行到目标行
     *
     * @param toRowIndex int 目标行索引，非行号
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T copyTo(final int toRowIndex) {
        final Sheet sheet = getSheet();
        final Row srcRow = getRow();
        if (sheet instanceof XSSFSheet) {
            ((XSSFSheet) sheet).copyRows(Arrays.asList(srcRow), toRowIndex, getOps().cellCopyPolicy);
        } else if (sheet instanceof SXSSFSheet) {
            // 参考:org.apache.poi.xssf.usermodel.XSSFRow > copyRowFrom
            // 自定义实现 SXSSFSheet 复制行操作
            // 只支持单行复制
            // 公式列只支持替换行号
            final CellCopyPolicy policy = getOps().cellCopyPolicy;
            final SXSSFRow destRow = (SXSSFRow) sheet.createRow(toRowIndex);
            srcRow.forEach(srcCell -> { // 循环复制单元格
                SXSSFCell destCell = destRow.createCell(srcCell.getColumnIndex(), srcCell.getCellTypeEnum());
                { // 参考: org.apache.poi.xssf.usermodel.XSSFCell > copyCellFrom
                    if (policy.isCopyCellValue()) {
                        CellType copyCellType = srcCell.getCellTypeEnum();
                        if (copyCellType == CellType.FORMULA && !policy.isCopyCellFormula())
                            copyCellType = srcCell.getCachedFormulaResultTypeEnum();
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
                                throw new IllegalArgumentException("Invalid cell type " + srcCell.getCellTypeEnum());
                        }
                    }
                    if (policy.isCopyCellStyle()) {
                        destCell.setCellStyle(srcCell.getCellStyle());
                    }
                    Hyperlink srcHyperlink = srcCell.getHyperlink();
                    if (policy.isMergeHyperlink()) {
                        if (srcHyperlink != null) {
                            destCell.setHyperlink(new XSSFHyperlink(srcHyperlink));
                        }
                    } else if (policy.isCopyHyperlink()) {
                        destCell.setHyperlink(srcHyperlink == null ? null : new XSSFHyperlink(srcHyperlink));
                    }
                }
            });
            if (policy.isCopyRowHeight()) {
                destRow.setHeight(srcRow.getHeight());
            }
            if (policy.isCopyMergedRegions()) {
                sheet.getMergedRegions().forEach(cellRangeAddress -> {
                    int shift = destRow.getRowNum() - srcRow.getRowNum();
                    if (srcRow.getRowNum() == cellRangeAddress.getFirstRow() && cellRangeAddress.getLastRow() == srcRow.getRowNum()) {
                        CellRangeAddress destRegion = cellRangeAddress.copy();
                        destRegion.setFirstRow(destRegion.getFirstRow() + shift);
                        destRegion.setLastRow(destRegion.getLastRow() + shift);
                        sheet.addMergedRegion(destRegion);
                    }
                });
            }
//            throw new RuntimeException(getSheet().getClass().toString()+" 不支持复制操作");
        }
        return (T) this;
    }
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
    default <T extends ISheetWriter> T copyToNext() {
        // row.getRowNum() 原生XSSFRow 拿到的 RowNum 实际上是行索引，不是行号
        copyTo(getRow().getRowNum() + 1);
        return (T) this;
    }

    /**
     * 清除当前行所有单元格内容，跳过公式，公式列不清除；保留单元格样式
     *
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T clearRowContent() {
        getRow().forEach(cell -> {
            // 公式列跳过，不执行清除操作
            if (CellType.FORMULA != cell.getCellTypeEnum()) cell.setCellType(CellType.BLANK);
        });
        return (T) this;
    }

    /**
     * 清除当前单元格内容，跳过公式，公式列不清除；保留单元格样式
     *
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T clearCellContent() {
        getCell().setCellType(CellType.BLANK);
        return (T) this;
    }

    /**
     * 向当前行所有列追加样式
     *
     * @param cellStyles CellStyles 将此样式追加到所有列
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T appendStyleOfRow(@NonNull final CellStyles cellStyles) {
        getRow().forEach(cell -> {
//            // 当索引为 0 ，表示未附加任何样式，需要新建样式
//            if (0 == cell.getCellStyle().getIndex()) cell.setCellStyle(cellStyles.createCellStyle(workbook));
//            else cell.setCellStyle(cellStyles.appendClone(workbook, (CellStyle) cell.getCellStyle()));
            cell.setCellStyle(cellStyles.appendClone(getWorkbook(), cell.getCellStyle()));
        });
        return (T) this;
    }

    /**
     * 向当前行指定列追加样式
     *
     * @param cellStyles CellStyles 将此样式追加到所有列
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T appendStyle(@Nonnull final CellStyles cellStyles) {
//       // 当索引为 0 ，表示未附加任何e样式，需要新建样式
//            if (0 == cell.getCellStyl().getIndex()) cell.setCellStyle(cellStyles.createCellStyle(workbook));
//            else cell.setCellStyle(cellStyles.appendClone(workbook, (CellStyle) cell.getCellStyle()));
        getCell().setCellStyle(cellStyles.appendClone(getWorkbook(), getCell().getCellStyle()));
        return (T) this;
    }

    /**
     * 通过 Rownum 对象指定当前操作行
     *
     * @param rownum Rownum
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T row(final Rownum rownum) {
        return row(rownum.rowIndex());
    }

    /**
     * 通过行索引指定当前操作行
     *
     * @param rowIndex int 行索引，非行号
     * @return <T extends ISheetWriter>
     */
    <T extends ISheetWriter> T row(final int rowIndex);

    /**
     * 指定当前操作行
     *
     * @param row Row 数据行
     * @return <T extends ISheetWriter>
     */
    <T extends ISheetWriter> T row(@NonNull final Row row);

    /**
     * 指定当前操作列
     *
     * @param position Position 单元格坐标
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T cell(@NonNull final Position position) {
        row(position.rowIndex()).cell(position.columnIndex());
        return (T) this;
    }

    /**
     * 指定当前操作列
     *
     * @param columnIndex int 列索引，非行号
     * @return <T extends ISheetWriter>
     */
    <T extends ISheetWriter> T cell(final int columnIndex);

    /**
     * 向当前单元格写入数据
     *
     * @param data Cell 写入数据单元格对象
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T write(final com.utils.common.entity.excel.Cell data) {
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
    default <T extends ISheetWriter> T writeText(final String value) {
        Cell cell = getCell();
        if (Objects.isNull(value)) cell.setCellType(CellType.BLANK);
        else {
            cell.setCellType(CellType.STRING);
            cell.setCellValue(value);
        }
        return (T) this;
    }

    /**
     * 向当前单元格写入数字
     *
     * @param value Number 数字
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T writeNumber(final Number value) {
        Cell cell = getCell();
        if (Objects.isNull(value)) cell.setCellType(CellType.BLANK); // 设置为 BLANK 可以清空单元格内容，保留样式
        else {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue(value.doubleValue());
        }
        return (T) this;
    }

    /**
     * 向当前单元格写入日期，使用此方法必须通过 writeStyle 写入日期格式化样式
     *
     * @param value Date 日期
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T writeDate(final Date value) {
        Cell cell = getCell();
        if (Objects.isNull(value)) getCell().setCellType(CellType.BLANK); // 设置为 BLANK 可以清空单元格内容，保留样式
        else cell.setCellValue(value);
        return (T) this;
    }

    /**
     * 向当前单元格写入日期，使用此方法必须通过 writeStyle 写入日期格式化样式
     *
     * @param value Timestamp 日期
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T writeDate(final Timestamp value) {
        Cell cell = getCell();
        if (Objects.isNull(value)) getCell().setCellType(CellType.BLANK); // 设置为 BLANK 可以清空单元格内容，保留样式
        else cell.setCellValue(value);
        return (T) this;
    }

    /**
     * 向当前单元格写入日期
     *
     * @param value String 日期
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T writeDate(final String value) {
        Cell cell = getCell();
        if (Objects.isNull(value)) getCell().setCellType(CellType.BLANK); // 设置为 BLANK 可以清空单元格内容，保留样式
        else cell.setCellValue(value);
        return (T) this;
    }

    /**
     * 向当前单元格写入公式
     *
     * @param formula String 公式
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T writeFormula(final String formula) {
        if (Util.isEmpty(formula)) getCell().setCellType(CellType.BLANK);
        else {
            final Supplier<String> supplier = () -> { // 获取公式
                if (formula.indexOf("{0}") > 0) { // 当公式使用 {0} 占位行号时，将 {0} 替换成行号
                    return formula.replace("{0}", (getRow().getRowNum() + 1) + "");
                } else if (getOps().rebuildFormula) { // 判断如果开启公式重构，则执行公式重构方法
                    // 重构规则说明：假设当前行号为100
                    // 公式：A1+B1 > A100+B100
                    // 公式：SUM(A1:C1) > SUM(A100:C100)
                    // 公式：A1*C1 > A100*C100
                    // 公式：A1*C1-D1 > A100*C100-D100
                    // 公式(错误案例演示)：A1+A2+A3 > A100+A100+A100；因为：A1+A2+A3 属于跨行计算
                    // 公式(错误案例演示)：SUM(A1:A3) > SUM(A100:A100)；因为：A1:A3 属于跨行计算
                    // 以上案例说明，只支持横向的单行公式，不支持跨行和跨表
                    return formula.replaceAll("(((?<=.?[A-Z])(\\d{1,10})(?=\\D?.*))|(?<=.?[A-Z])(\\d{1,10})$)", (getRow().getRowNum() + 1) + "");
                }
                return formula;
            };
            getCell().setCellFormula(supplier.get());
        }
        return (T) this;
    }

    /**
     * 向当前单元格写入样式
     *
     * @param styleIndex int 样式索引，将会从样式库中获取样式
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T writeStyle(final int styleIndex) {
        writeStyle(getOps().cloneStyles.clone(styleIndex));
        return (T) this;
    }

    /**
     * 向当前单元格写入样式
     *
     * @param cellStyle CellStyle 样式
     * @return <T extends ISheetWriter>
     */
    default <T extends ISheetWriter> T writeStyle(final CellStyle cellStyle) {
        if (Objects.nonNull(cellStyle)) getCell().setCellStyle(cellStyle);
        return (T) this;
    }

//    /**
//     * 强制刷新公式，自动调整列宽
//     *
//     * @return <T extends ISheetWriter>
//     */
//    default <T extends ISheetWriter> T flush() {
//        XSSFFormulaEvaluator.evaluateAllFormulaCells(getWorkbook());
//        Sheet sheet = getSheet();
//        for (Cell cell : sheet.getRow(0)) {
//            sheet.autoSizeColumn(cell.getColumnIndex());
//        }
//        return (T) this;
//    }
}