package com.utils.util.excel;

import com.utils.common.entity.excel.Cell;
import com.utils.enums.Colors;
import com.utils.enums.DataType;
import com.utils.util.Asserts;
import com.utils.util.Dates;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;

/**
 * 【.xlsx】数据写入操作
 *
 * @author Jason Xie on 2018-8-8.
 */
@Slf4j
public class XSheetWriter implements ISheetWriter<XSheetWriter>, ISheetWriter.ICopyRows<XSheetWriter> {
    private XSheetWriter(final XSSFSheet sheet, final Options ops) {
        this.ops = Objects.isNull(ops) ? Options.builder().build() : ops;
        this.workbook = sheet.getWorkbook();
        this.sheet = sheet;
//        this.sheet.setForceFormulaRecalculation(true); // 设置强制刷新公式
        // 若此上面一行设置不起作用，则在写入文件之前使用这行代码强制刷新公式：XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
    }

    public static XSheetWriter of(final XSSFSheet sheet) {
        return of(sheet, null);
    }

    public static XSheetWriter of(final XSSFSheet sheet, final Options ops) {
        Asserts.notEmpty(sheet, "参数【sheet】是必须的");
        return new XSheetWriter(sheet, ops);
    }

    @Getter
    private final Options ops;
    private final XSSFWorkbook workbook;
    private final XSSFSheet sheet;
    /**
     * 当前操作行索引
     */
    @Getter
    private int rowIndex;
    /**
     * 当前操作行
     */
    private XSSFRow row;
    /**
     * 当前操作单元格
     */
    private XSSFCell cell;
    private ExcelReader reader;

    public ExcelReader getReader() {
        if (Objects.isNull(reader)) reader = ExcelReader.of(sheet);
        return reader;
    }

    @Override
    public Workbook getWorkbook() {
        return this.workbook;
    }

    @Override
    public Sheet getSheet() {
        return this.sheet;
    }

    @Override
    public Row getRow() {
        return this.row;
    }

    @Override
    public org.apache.poi.ss.usermodel.Cell getCell() {
        return this.cell;
    }

    @Override
    public XSheetWriter setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
        return this;
    }

    @Override
    public XSheetWriter row(final int rowIndex) {
        row(Optional.ofNullable(sheet.getRow(rowIndex))
                .orElseGet(() -> sheet.createRow(rowIndex))
        );
        return this;
    }

    @Override
    public XSheetWriter row(@NonNull final Row row) {
        return row((XSSFRow) row);
    }

    /**
     * 指定当前操作行
     *
     * @param row XSSFRow 数据行
     * @return XSheetWriter
     */
    public XSheetWriter row(@NonNull final XSSFRow row) {
        if (Objects.nonNull(row)) rowIndex = row.getRowNum();
        this.row = row;
        this.cell = null; // 切换行，需要将 cell 置空
        return this;
    }

    @Override
    public XSheetWriter cell(final int columnIndex) {
        cellOfNew(columnIndex);
        return this;
    }

    @Override
    public XSheetWriter cell(final org.apache.poi.ss.usermodel.Cell cell) {
        this.cell = (XSSFCell) cell;
        return this;
    }

    @Override
    public XSheetWriter copyTo(int toRowIndex) {
        sheet.copyRows(Arrays.asList(row), toRowIndex, ops.cellCopyPolicy);
        return this;
    }

    public static void main(String[] args) {
        Paths.get("logs").toFile().mkdir();
        { // 测试公式行号替换
//            Supplier supplier = () -> {
//                String reg = "(((?<=.?[A-Z])(\\d{1,10})(?=\\D?.*))|(?<=.?[A-Z])(\\d{1,10})$)";
//                Stream.of(
//                        "A10",
//                        "IF1235A:A121=fdsaC123-B122=IF1235A",
//                        "IF1235A:A121=fdsaC123-B122=IF1235",
//                        "IF1235A:A121=fdsaC123-B122",
//                        "A8888+123+B8888*2*C8888+123",
//                        "A8888:AC123+123+AB8888*2*C8888+123",
//                        "SUM(A8888:C8888)"
//                ).forEach(formula -> {
//                    log.debug("\n"+formula + " > \n" + formula.replaceAll(reg, "1000"));
//                });
//                return null;
//            };
//            supplier.get();
        }
        List<Cell> cellDatas = new ArrayList<>();
        { // 普通写入
            Supplier supplier = () -> {
                try {
                    Rownum rownum = Rownum.of(1); // 从第几行开始写
                    @Cleanup XSSFWorkbook workbook = new XSSFWorkbook();
                    workbook.createSheet("Sheet1");
                    workbook.getSheetAt(0).setDefaultColumnWidth(15);
                    // 日期格式样式
                    CellStyle dateStyle = CellStyles.builder().dataFormat(workbook.createDataFormat().getFormat(Dates.Pattern.yyyy_MM_dd.value())).build().createCellStyle(workbook);
                    // 蓝色单元格样式
                    CellStyle blueStyle = CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.SkyBlue.color).build().createCellStyle(workbook);
                    // 绿色单元格样式
                    CellStyle greenStyle = CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.BrightGreen.color).build().createCellStyle(workbook);
                    // 红色单元格样式
                    CellStyle redStyle = CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.Red.color).build().createCellStyle(workbook);
                    // 文本居中样式，可追加的样式对象
                    CellStyles centerStyle = CellStyles.builder().alignment(HorizontalAlignment.CENTER).build();
                    { // 后面 >>>>>> 测试样式库引用 <<<<<< 会使用到
                        cellDatas.add(Cell.builder().sindex((int) greenStyle.getIndex()).text("绿色单元格").type(DataType.TEXT).build());
                        cellDatas.add(Cell.builder().sindex((int) blueStyle.getIndex()).text("蓝色单元格").type(DataType.TEXT).build());
                        cellDatas.add(Cell.builder().sindex((int) redStyle.getIndex()).text("红色单元格").type(DataType.TEXT).build());
                    }
                    XSheetWriter sheetWriter = XSheetWriter.of(workbook.getSheetAt(0))
                            .row(rownum)
                            .cell(0).writeNumber(100)
                            .cell(1).writeFormula("100*A1")
                            .cell(2).writeDate(Dates.now().formatDate())
                            .cell(3).writeText("蓝色单元格").writeStyle(blueStyle)
                            .cell(4).writeText("绿色单元格").writeStyle(greenStyle)
                            .cell(5).writeText("红色单元格").writeStyle(redStyle)
                            .appendStyleOfRow(centerStyle) // 当前行所有列追加文本居中样式
                            .row(rownum.next())
                            .cell(0).writeNumber(100).appendStyle(centerStyle) // 当前行指定列追加文本居中样式
                            .cell(1).writeFormula("100*A2")
                            .cell(2).writeDate(Dates.now().date()).writeStyle(dateStyle)
                            .cell(3).writeText("蓝色单元格").writeStyle(blueStyle).appendStyle(centerStyle) // 当前行指定列追加文本居中样式
                            .cell(4).writeText("绿色单元格").writeStyle(greenStyle)
                            .cell(5).writeText("红色单元格").writeStyle(redStyle);
                    { // 测试 FillPatternType
                        FillPatternType[] types = FillPatternType.values();
                        sheetWriter.row(rownum.next());
                        for (int i = 0; i < types.length; i++) {
                            sheetWriter.cell(i)
                                    .writeText(types[i].name())
                                    .writeStyle(CellStyles.builder()
                                            .fillPattern(types[i])
                                            .fillBackgroundColor(Colors.Red.color)
                                            .build()
                                            .createCellStyle(workbook)
                                    );
                        }
                        sheetWriter.row(rownum.next());
                        for (int i = 0; i < types.length; i++) {
                            sheetWriter.cell(i)
                                    .writeText(types[i].name())
                                    .writeStyle(CellStyles.builder() // 绿色单元格样式
                                            .fillPattern(types[i])
                                            .fillForegroundColor(Colors.BrightGreen.color)
                                            .build()
                                            .createCellStyle(workbook)
                                    );
                        }
                    }
                    Path path = Paths.get("logs", "普通写入.xlsx").toAbsolutePath();
                    @Cleanup FileOutputStream fileOutputStream = new FileOutputStream(path.toFile());
                    workbook.write(fileOutputStream);
                    log.debug("写入路径：" + path.toAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            };
            supplier.get();
        }
        { // 复制
            Supplier supplier = () -> {
                try {
                    Rownum rownum = Rownum.of(1); // 从第几行开始写
                    @Cleanup XSSFWorkbook workbook = new XSSFWorkbook();
                    workbook.createSheet("Sheet1");
                    workbook.getSheetAt(0).setDefaultColumnWidth(15);
                    // 日期格式样式
                    CellStyle dateStyle = CellStyles.builder().dataFormat(workbook.createDataFormat().getFormat(Dates.Pattern.yyyy_MM_dd.value())).build().createCellStyle(workbook);
                    // 蓝色单元格样式
                    CellStyle blueStyle = CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.SkyBlue.color).build().createCellStyle(workbook);
                    // 绿色单元格样式
                    CellStyle greenStyle = CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.BrightGreen.color).build().createCellStyle(workbook);
                    // 红色单元格样式
                    CellStyle redStyle = CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.Red.color).build().createCellStyle(workbook);
                    // 文本居中样式，可追加的样式对象
                    CellStyles centerStyle = CellStyles.builder().alignment(HorizontalAlignment.CENTER).build();
                    XSheetWriter sheetWriter = XSheetWriter.of(workbook.getSheetAt(0))
                            .row(rownum)
                            .cell(0).writeText("第1行")
                            .cell(1).writeNumber(100)
                            .cell(2).writeNumber(1)
                            .cell(3).writeFormula("B1*C1")
                            .cell(4).writeDate(Dates.now().date()).writeStyle(dateStyle)
                            .cell(5).writeText("蓝色单元格").writeStyle(blueStyle)
                            .cell(6).writeText("绿色单元格").writeStyle(greenStyle)
                            .cell(7).writeText("红色单元格").writeStyle(redStyle)
                            .appendStyleOfRow(centerStyle) // 当前行所有列追加文本居中样式
                            .copyToNext() // 复制到下一行

                            .row(rownum.next())
                            .cell(0).writeText("第2行：从第1行复制来的")
                            .cell(1).writeNumber(101)
                            .cell(2).writeNumber(2)
//                            .cell(3).writeFormula("B1*C1") // 执行复制行时会自动复制公式
                            .cell(4).writeDate(Dates.now().addDay(1).date())
//                            .cell(5).writeText("蓝色单元格")
//                            .cell(6).writeText("绿色单元格")
//                            .cell(7).writeText("红色单元格")
                            ;
//                    // 设置第二行的红色单元格与后面一列合并，测试合并列是否会被复制
                    sheetWriter.getSheet().addMergedRegion(new CellRangeAddress(Rownum.of(2).rowIndex(), Rownum.of(2).rowIndex(), 7, 8));
                    sheetWriter
                            // 从第 2 行复制到第 3 行, 复制都是使用索引
                            .copy(Rownum.of(2).rowIndex(), Rownum.of(3).rowIndex())
                            .row(rownum.next())
                            .cell(0).writeText("第3行：从第2行复制来的")
                            .cell(2).writeNumber(3)
                            .cell(3).writeFormula("100*B3")
                            // 将第 3 行复制，从第 6 行开始作为目标行，总共复制 5 行, 复制都是使用索引
                            .copy(Rownum.of(3).rowIndex(), Rownum.of(6).rowIndex(), 5)
                            // 切换到第6行
                            .row(rownum.set(6))
                            .cell(0).writeText("第6行：从第3行复制来的，清除红色单元格文字")
                            .cell(7).setCellBlank()

                            .row(rownum.next())
                            .cell(0).writeText("第7行：从第3行复制来的，清除绿色单元格文字")
                            .cell(6).setCellBlank()

                            .row(rownum.next())
                            .cell(1).appendStyle(CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.Red.color).build())
                            .setRowBlankIgnoreFromula() // 清除整行数据，公式不清除
                            .cell(0).writeText("第8行：从第3行复制来的,清除整行数据，公式不清除")

                            .row(rownum.next())
                            .cell(1).appendStyle(CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.Red.color).build()).writeNumber(null)
                            .setRowBlank() // 清除整行数据，公式会被清除
                            .cell(0).writeText("第9行：从第3行复制来的,清除整行数据，公式会被清除")

                            .row(rownum.next())
                            .cell(0).writeText("第10行：从第3行复制来的")

                            .row(rownum.next())
                            .cell(0).writeText("第11行");
                    Path path = Paths.get("logs", "复制.xlsx").toAbsolutePath();
                    @Cleanup FileOutputStream fileOutputStream = new FileOutputStream(path.toFile());
                    workbook.write(fileOutputStream);
                    log.debug("写入路径：" + path.toAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            };
            supplier.get();
        }
        { // 公式重构
            Supplier supplier = () -> {
                try {
                    Rownum rownum = Rownum.of(1); // 从第几行开始写
                    @Cleanup XSSFWorkbook workbook = new XSSFWorkbook();
                    workbook.createSheet("Sheet1");
                    workbook.getSheetAt(0).setDefaultColumnWidth(15);
                    XSheetWriter.of(workbook.getSheetAt(0)).row(rownum);
                    XSheetWriter.of(workbook.getSheetAt(0),
                            Options.builder().rebuildFormula(true).build() // 开启公式重构
                    )
                            .row(rownum)
                            .cell(0).writeNumber(100)
                            .cell(1).writeNumber(2)
                            .cell(2).writeFormula("A9*B9") // 将公式以当前实际行号重构，这里写入的公式实际会变成 A1*B1
                    ;
                    Path path = Paths.get("logs", "公式重构.xlsx").toAbsolutePath();
                    @Cleanup FileOutputStream fileOutputStream = new FileOutputStream(path.toFile());
                    workbook.write(fileOutputStream);
                    log.debug("写入路径：" + path.toAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            };
            supplier.get();
        }
        { // 指定样式来源，测试样式库引用
            Supplier supplier = () -> {
                try {
                    Rownum rownum = Rownum.of(1); // 从第几行开始写
                    @Cleanup XSSFWorkbook workbook = new XSSFWorkbook();
                    workbook.createSheet("Sheet1");
                    workbook.getSheetAt(0).setDefaultColumnWidth(15);
                    XSheetWriter.of(workbook.getSheetAt(0))
                            .setCloneStyles(Paths.get("logs", "普通写入.xlsx").toAbsolutePath().toString()) // 指定引用样式库文件路径
                            .row(rownum)
                            .cell(0).write(cellDatas.get(0))
                            .cell(1).write(cellDatas.get(1))
                            .cell(2).write(cellDatas.get(2))
                    ;
                    Path path = Paths.get("logs", "样式库引用【普通写入】.xlsx").toAbsolutePath();
                    @Cleanup FileOutputStream fileOutputStream = new FileOutputStream(path.toFile());
                    workbook.write(fileOutputStream);
                    log.debug("写入路径：" + path.toAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            };
            supplier.get();
        }
    }
}