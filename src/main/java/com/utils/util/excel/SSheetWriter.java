package com.utils.util.excel;

import com.utils.common.entity.excel.Cell;
import com.utils.common.entity.excel.Rownum;
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
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * excel > sheet 写入操作
 *
 * @author Jason Xie on 2017/11/7.
 */
@Slf4j
public class SSheetWriter implements ISheetWriter<SSheetWriter> {
    private SSheetWriter(final SXSSFSheet sheet, final Options ops) {
        this.ops = Objects.isNull(ops) ? Options.builder().build() : ops;
        this.workbook = sheet.getWorkbook();
        this.sheet = sheet;
        this.sheet.trackAllColumnsForAutoSizing(); // 自动调整列宽
//        this.sheet.setForceFormulaRecalculation(true); // 设置强制刷新公式
        // 若此上面一行设置不起作用，则在写入文件之前使用这行代码强制刷新公式： SXSSFFormulaEvaluator.evaluateAllFormulaCells(workbook, true);
    }

    public static SSheetWriter of(final SXSSFSheet sheet) {
        return of(sheet, null);
    }
    public static SSheetWriter of(final SXSSFSheet sheet, final Options ops) {
        Asserts.notEmpty(sheet, "参数【sheet】是必须的");
        return new SSheetWriter(sheet, ops);
    }

    @Getter
    private final Options ops;
    private final SXSSFWorkbook workbook;
    private final SXSSFSheet sheet;
    /**
     * 当前操作行
     */
    private SXSSFRow row;
    /**
     * 当前操作列
     */
    private SXSSFCell cell;
    private ExcelReader reader;

    public ExcelReader getReader() {
        if(Objects.isNull(reader)) reader = ExcelReader.of(sheet);
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
    public SSheetWriter row(final int rowIndex) {
        this.row = sheet.getRow(rowIndex);
        if (Objects.isNull(row)) this.row = sheet.createRow(rowIndex);
        this.cell = null; // 切换行，需要将 cell 置空
        return this;
    }
    @Override
    public SSheetWriter row(@NonNull final Row row) {
        return row((SXSSFRow) row);
    }
    /**
     * 指定当前操作行
     *
     * @param row XSSFRow 数据行
     * @return SSheetWriter
     */
    public SSheetWriter row(@NonNull final SXSSFRow row) {
        this.row = row;
        this.cell = null; // 切换行，需要将 cell 置空
        return this;
    }
    @Override
    public SSheetWriter cell(final int columnIndex) {
        this.cell = row.getCell(columnIndex);
        if (Objects.isNull(this.cell)) this.cell = row.createCell(columnIndex, CellType.BLANK);
        return this;
    }

    public static void main(String[] args) {
        Paths.get("logs").toFile().mkdir();
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
                            .cell(0).writeNumber(100)
                            .cell(1).writeNumber(1)
                            .cell(2).writeFormula("A1*B1")
                            .cell(3).writeDate(Dates.now().date()).writeStyle(dateStyle)
                            .cell(4).writeText("蓝色单元格").writeStyle(blueStyle)
                            .cell(5).writeText("绿色单元格").writeStyle(greenStyle)
                            .cell(6).writeText("红色单元格").writeStyle(redStyle)
                            .appendStyleOfRow(centerStyle) // 当前行所有列追加文本居中样式
                            .copyToNext() // 复制到下一行

                            .row(rownum.next())
                            .cell(0).writeNumber(101)
                            .cell(1).writeNumber(2)
//                            .cell(2).writeFormula("A1*B1") // 执行复制行时会自动复制公式
                            .cell(3).writeDate(Dates.now().addDay(1).date())
                            .cell(4).writeText("蓝色单元格")
                            .cell(5).writeText("绿色单元格")
                            .cell(6).writeText("红色单元格");
                    // 设置第二行的红色单元格与后面一列合并，测试合并列是否会被复制
                    sheetWriter.getSheet().addMergedRegion(new CellRangeAddress(Rownum.of(2).rowIndex(), Rownum.of(2).rowIndex(), 6, 7));
                    sheetWriter.copy(Rownum.of(2).rowIndex(), Rownum.of(3).rowIndex())  // 从第 2 行复制到第 3 行, 复制都是使用索引
                            .row(rownum.next())
                            .cell(1).writeNumber(3)
                            .cell(2).writeFormula("100*B3")
                    ;
                    sheetWriter.copy(Rownum.of(3).rowIndex(), Rownum.of(6).rowIndex(), 5);  // 将第 3 行复制，从第 6 行开始作为目标行，总共复制 5 行, 复制都是使用索引
                    sheetWriter.row(rownum.next())
                            .cell(6).writeText(null)

                            .row(rownum.next())
                            .cell(6).writeText(null)

                            .row(rownum.next())
                            .cell(0).appendStyle(CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.Red.color).build())
                            .clearRowContent() // 清除整行数据，公式不清除

                            .row(rownum.next())
                            .cell(0).appendStyle(CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.Red.color).build()).writeNumber(null)
                            .cell(2).clearCellContent() // 清除列数据，公式会被清除
                            .cell(6).writeText(null)

                            .row(rownum.next())
                            .cell(6).writeText(null)

                            .row(rownum.next())
                            .cell(6).writeText(null);
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