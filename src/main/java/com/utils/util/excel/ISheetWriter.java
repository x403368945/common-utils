package com.utils.util.excel;

import lombok.Builder;
import lombok.Cleanup;
import lombok.NonNull;
import org.apache.poi.hssf.usermodel.HSSFSheet;
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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

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
        /**
         * 是否对公式执行 rebuild 操作
         */
        @Builder.Default
        boolean rebuildFormula = false;
    }

    /**
     * POI Excel 复制行规则，默认设置，会复制单元格值
     */
    Options getOps();

    Workbook getWorkbook();


    /**
     * 指定样式库
     *
     * @param stylesTable StylesTable 样式库
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
     * 新建操作行
     *
     * @param rowIndex int 行索引
     * @return <T extends ISheetWriter>
     */
    default T rowNew(final int rowIndex) {
        return row(getSheet().createRow(rowIndex));
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
            switch (cell.getCellTypeEnum()) {
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
        cell(Optional
                .ofNullable(getRow().getCell(columnIndex))
                .orElseGet(() -> getRow().createCell(columnIndex, CellType.BLANK))
        );
        return (T) this;
    }

    /**
     * 新建操作单元格
     *
     * @param columnIndex int 列索引
     * @return <T extends ISheetWriter>
     */
    default T cellNew(final int columnIndex) {
        cell(getRow().createCell(columnIndex, CellType.BLANK));
        return (T) this;
    }


    /**
     * 向当前行所有列追加样式
     *
     * @param cellStyles CellStyles 将此样式追加到所有列
     * @return <T extends ISheetWriter>
     */
    default T appendStyleOfRow(@NonNull final CellStyles cellStyles) {
        for (int i = 0; i < getRow().getLastCellNum(); i++) {
            cellOfNew(i).getCell().setCellStyle(cellStyles.appendClone(getSheet().getWorkbook(), getCell().getCellStyle()));
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
     * 向当前单元格写入公式
     *
     * @param formula String 公式
     * @return <T extends ISheetWriter>
     */
    default T writeFormula(final String formula) {
        writeFormula(() -> { // 获取公式
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
        });
        return (T) this;
    }

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
     * 自动调整列宽
     *
     * @return <T extends ISheetWriter>
     */
    default T autoColumnWidth() {
        final Sheet sheet = getSheet();
        for (Cell cell : sheet.getRow(0)) {
            sheet.autoSizeColumn(cell.getColumnIndex());
        }
        return (T) this;
    }

    /**
     * 不同版本的 excel 执行复制操作相关的方法封装
     *
     * @param <T>
     */
    interface ICopyRows<T> {
        CellCopyPolicy defaultCellCopyPolicy = new CellCopyPolicy().createBuilder().build();

        interface ICopy {
            void copy(final Sheet sheet,
                      final int fromStratRowIndex,
                      final int fromEndRowIndex,
                      final int toRowIndex,
                      final int repeatCount,
                      CellCopyPolicy cellCopyPolicy);
        }

        enum SheetTypes {
            XSSFSHEET(".xlsx",
                    (sheet) -> sheet instanceof XSSFSheet,
                    (sheet, fromStratRowIndex, fromEndRowIndex, toRowIndex, repeatCount, cellCopyPolicy) -> {
                        // 实现 .xlsx 行复制功能
                        // Sheet sheet, int fromStratRowIndex, int fromEndRowIndex, int toRowIndex, int repeatCount, CellCopyPolicy cellCopyPolicy
                        if (Objects.isNull(cellCopyPolicy)) cellCopyPolicy = defaultCellCopyPolicy;
                        final XSSFSheet xsheet = (XSSFSheet) sheet;
                        for (int i = 0; i < repeatCount; i++) {
                            xsheet.copyRows(fromStratRowIndex, fromEndRowIndex, toRowIndex + i + (i * (fromEndRowIndex - fromStratRowIndex)), cellCopyPolicy);
                        }
                    }),
            SXSSFSHEET(".xlsx限制最大缓存写入",
                    (sheet) -> sheet instanceof SXSSFSheet,
                    (sheet, fromStratRowIndex, fromEndRowIndex, toRowIndex, repeatCount, cellCopyPolicy) -> {
                        // 实现 .xlsx 带最大缓存航的 行复制功能
                        // 本身并不支持复制操作，尝试实现复制操作
//                        Stream.iterate(0, v -> v + 1).limit(repeatCount).forEach(i -> { });
                        final CellCopyPolicy policy = Objects.isNull(cellCopyPolicy) ? defaultCellCopyPolicy : cellCopyPolicy;
                        for (int i = 0; i < repeatCount; i++) {
                            for (int j = fromStratRowIndex; j <= fromEndRowIndex; j++) {
                                // 参考:org.apache.poi.xssf.usermodel.XSSFRow#copyRowFrom
                                // 自定义实现 SXSSFSheet 复制行操作
                                // 只支持单行复制，且公式列只支持号替换行
                                final SXSSFRow srcRow = ((SXSSFSheet) sheet).getRow(fromStratRowIndex);
                                final SXSSFRow destRow = ((SXSSFSheet) sheet).createRow(toRowIndex + i + (i * (fromEndRowIndex - fromStratRowIndex)));
                                srcRow.forEach(srcCell -> { // 循环复制单元格
                                    final SXSSFCell destCell = destRow.createCell(srcCell.getColumnIndex(), srcCell.getCellTypeEnum());
                                    { // 参考: org.apache.poi.xssf.usermodel.XSSFCell > copyCellFrom
                                        if (policy.isCopyCellValue()) {
                                            CellType copyCellType = srcCell.getCellTypeEnum();
                                            if (copyCellType == CellType.FORMULA && !policy.isCopyCellFormula())
                                                copyCellType = srcCell.getCachedFormulaResultTypeEnum();

                                            switch (copyCellType) {
                                                case NUMERIC:
                                                    if (DateUtil.isCellDateFormatted(srcCell))
                                                        destCell.setCellValue(srcCell.getDateCellValue());
                                                    else destCell.setCellValue(srcCell.getNumericCellValue());
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
                                            if (Objects.nonNull(srcHyperlink))
                                                destCell.setHyperlink(new XSSFHyperlink(srcHyperlink));
                                        } else if (policy.isCopyHyperlink()) {
                                            if (Objects.nonNull(srcHyperlink))
                                                destCell.setHyperlink(new XSSFHyperlink(srcHyperlink));
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
            HSSFSHEET(".xls",
                    (sheet) -> sheet instanceof HSSFSheet,
                    (sheet, fromStratRowIndex, fromEndRowIndex, toRowIndex, repeatCount, cellCopyPolicy) -> {
                        // 实现 .xls 行复制功能

                    }),;
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
         * @param cellCopyPolicy CellCopyPolicy POI Excel 复制行规则；只有 XSSFWorkbook 支持行复制操作
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
         * @param row        Row 指定行
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