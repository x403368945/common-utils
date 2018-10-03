package com.utils.excel;

import com.utils.enums.Colors;
import com.utils.excel.entity.Cell;
import com.utils.excel.entity.Position;
import com.utils.excel.entity.Range;
import com.utils.excel.enums.DataType;
import com.utils.util.Dates;
import com.utils.util.FPath;
import lombok.Cleanup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 【.xlsx】限定最大缓行的数据写入操作；写入完成之后需要调用 close 方法；否则可能造成内存泄露
 *
 * @author Jason Xie on 2018-8-8.
 */
@Slf4j
public class SSheetWriter implements ISheetWriter<SSheetWriter>, ISheetWriter.ICopyRows<SSheetWriter> {
    private SSheetWriter(final SXSSFSheet sheet, final Options ops) {
        this.ops = Objects.isNull(ops) ? Options.builder().build() : ops;
        this.workbook = sheet.getWorkbook();
        this.sheet = sheet;
        this.sheet.trackAllColumnsForAutoSizing(); // 自动调整列宽
//        this.sheet.setForceFormulaRecalculation(true); // 设置强制刷新公式
        // 若此上面一行设置不起作用，则在写入文件之前使用这行代码强制刷新公式： SXSSFFormulaEvaluator.evaluateAllFormulaCells(workbook, true);
    }

    public static SSheetWriter ofNewWorkBook(final String sheetName) {
        final SXSSFWorkbook wb = new SXSSFWorkbook();
        wb.createSheet(sheetName);
        return of(wb.getSheet(sheetName));
    }

    public static SSheetWriter of(final SXSSFSheet sheet) {
        return of(sheet, null);
    }

    public static SSheetWriter of(final SXSSFSheet sheet, final Options ops) {
        Objects.requireNonNull(sheet, "参数【sheet】是必须的");
        return new SSheetWriter(sheet, ops);
    }

    @Getter
    private final Options ops;
    private final SXSSFWorkbook workbook;
    private final SXSSFSheet sheet;
    /**
     * 当前操作行索引
     */
    @Getter
    private int rowIndex;
    /**
     * 当前操作行
     */
    private SXSSFRow row;
    /**
     * 当前操作单元格
     */
    private SXSSFCell cell;

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
    public SSheetWriter setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
        return this;
    }

    @Override
    public SSheetWriter row(final int rowIndex) {
        rowOfNew(rowIndex);
        return this;
    }

    @Override
    public SSheetWriter row(final Row row) {
        Objects.requireNonNull(row, "参数【row】是必须的");
        return row((SXSSFRow) row);
    }

    /**
     * 指定当前操作行
     *
     * @param row {@link SXSSFRow} 数据行
     * @return {@link SSheetWriter}
     */
    public SSheetWriter row(final SXSSFRow row) {
        Objects.requireNonNull(row, "参数【row】是必须的");
        rowIndex = row.getRowNum();
        this.row = row;
        this.cell = null; // 切换行，需要将 cell 置空
        return this;
    }

    @Override
    public SSheetWriter cell(final int columnIndex) {
        cellOfNew(columnIndex);
        return this;
    }

    @Override
    public SSheetWriter cell(final org.apache.poi.ss.usermodel.Cell cell) {
        this.cell = (SXSSFCell) cell;
        return this;
    }

    @Override
    public SSheetWriter copyTo(int toRowIndex) {
        copy(rowIndex, toRowIndex);
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
//                    log.info("\n"+formula + " > \n" + formula.replaceAll(reg, "1000"));
//                });
//                return null;
//            };
//            supplier.get();
        }
        List<Cell> cellDatas = new ArrayList<>();
        { // 普通写入
            Supplier supplier = () -> {
                try {
                    @Cleanup final SXSSFWorkbook workbook = new SXSSFWorkbook();
                    workbook.createSheet("Sheet1");
                    workbook.getSheet("Sheet1").setDefaultColumnWidth(15);
                    // 日期格式样式
                    final CellStyle dateStyle = CellStyles.builder().dataFormat(workbook.createDataFormat().getFormat(Dates.Pattern.yyyy_MM_dd.value())).build().createCellStyle(workbook);
                    // 蓝色单元格样式
                    final CellStyle blueStyle = CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.SkyBlue.color).build().createCellStyle(workbook);
                    // 绿色单元格样式
                    final CellStyle greenStyle = CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.BrightGreen.color).build().createCellStyle(workbook);
                    // 红色单元格样式
                    final CellStyle redStyle = CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.Red.color).build().createCellStyle(workbook);
                    // 文本居中样式，可追加的样式对象
                    final CellStyles centerStyle = CellStyles.builder().alignment(HorizontalAlignment.CENTER).build();
                    { // 后面 >>>>>> 测试样式库引用 <<<<<< 会使用到
                        cellDatas.add(Cell.builder().sindex((int) greenStyle.getIndex()).text("绿色单元格").type(DataType.TEXT).build());
                        cellDatas.add(Cell.builder().sindex((int) blueStyle.getIndex()).text("蓝色单元格").type(DataType.TEXT).build());
                        cellDatas.add(Cell.builder().sindex((int) redStyle.getIndex()).text("红色单元格").type(DataType.TEXT).build());
                    }
                    final SSheetWriter sheetWriter = SSheetWriter.of(workbook.getSheetAt(0))
                            .rowNew(Rownum.of(1))  // 从第1行开始写
                            .cellNew(0).writeNumber(100)
                            .cellNew(1).writeFormula("100*A1")
                            .cellNew(2).writeDate(Dates.now().formatDate())
                            .cellNew(3).writeText("蓝色单元格").writeStyle(blueStyle)
                            .cellNew(4).writeText("绿色单元格").writeStyle(greenStyle)
                            .cellNew(5).writeText("红色单元格").writeStyle(redStyle)
                            .appendStyleOfRow(centerStyle) // 当前行所有列追加文本居中样式
                            .nextRowNew()
                            .cellNew(0).writeNumber(100).appendStyle(centerStyle) // 当前行指定列追加文本居中样式
                            .cellNew(1).writeFormula("100*A2")
                            .cellNew(2).writeDate(Dates.now().date()).writeStyle(dateStyle)
                            .cellNew(3).writeText("蓝色单元格").writeStyle(blueStyle).appendStyle(centerStyle) // 当前行指定列追加文本居中样式
                            .cellNew(4).writeText("绿色单元格").writeStyle(greenStyle)
                            .cellNew(5).writeText("红色单元格").writeStyle(redStyle);
                    { // 测试 FillPatternType
                        final FillPatternType[] types = FillPatternType.values();
                        sheetWriter.nextRowNew();
                        for (int i = 0; i < types.length; i++) {
                            sheetWriter.cellNew(i)
                                    .writeText(types[i].name())
                                    .writeStyle(CellStyles.builder()
                                            .fillPattern(types[i])
                                            .fillBackgroundColor(Colors.Red.color)
                                            .build()
                                            .createCellStyle(workbook)
                                    );
                        }
                        sheetWriter.nextRowNew();
                        for (int i = 0; i < types.length; i++) {
                            sheetWriter.cellNew(i)
                                    .writeText(types[i].name())
                                    .writeStyle(CellStyles.builder() // 绿色单元格样式
                                            .fillPattern(types[i])
                                            .fillForegroundColor(Colors.BrightGreen.color)
                                            .build()
                                            .createCellStyle(workbook)
                                    );
                        }
                    }
                    log.info(String.format("写入路径：%s",
                            sheetWriter
//                                    .autoColumnWidth()
                                    .evaluateAllFormulaCells()
                                    .saveWorkBook(FPath.of("logs", "1.普通写入.xlsx")).absolute()
                    ));
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
                    @Cleanup final SXSSFWorkbook workbook = new SXSSFWorkbook();
                    workbook.createSheet("Sheet1");
                    workbook.getSheet("Sheet1").setDefaultColumnWidth(15);
                    // 日期格式样式
                    final CellStyle dateStyle = CellStyles.builder().dataFormat(workbook.createDataFormat().getFormat(Dates.Pattern.yyyy_MM_dd.value())).build().createCellStyle(workbook);
                    // 蓝色单元格样式
                    final CellStyle blueStyle = CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.SkyBlue.color).build().createCellStyle(workbook);
                    // 绿色单元格样式
                    final CellStyle greenStyle = CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.BrightGreen.color).build().createCellStyle(workbook);
                    // 红色单元格样式
                    final CellStyle redStyle = CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.Red.color).build().createCellStyle(workbook);
                    // 文本居中样式，可追加的样式对象
                    final CellStyles centerStyle = CellStyles.builder().alignment(HorizontalAlignment.CENTER).build();
                    final SSheetWriter sheetWriter = SSheetWriter.of(workbook.getSheetAt(0))
                            .rowNew(Rownum.of(1)) // 从第1行开始写
                            .cellNew(0).writeText("第1行")
                            .cellNew(1).writeNumber(100)
                            .cellNew(2).writeNumber(1)
                            .cellNew(3).writeFormula("B1*C1")
                            .cellNew(4).writeDate(Dates.now().date()).writeStyle(dateStyle)
                            .cellNew(5).writeText("蓝色单元格").writeStyle(blueStyle)
                            .cellNew(6).writeText("绿色单元格").writeStyle(greenStyle)
                            .cellNew(7).writeText("红色单元格").writeStyle(redStyle)
                            .appendStyleOfRow(centerStyle) // 当前行所有列追加文本居中样式
                            .copyToNext() // 复制到下一行

                            .nextRowOfNew()
                            .cell(0).writeText("第2行：从第1行复制来的")
                            .cell(1).writeNumber(101)
                            .cell(2).writeNumber(2)
//                            .cell(3).writeFormula("B1*C1") // 执行复制行时会自动复制公式
                            .cell(4).writeDate(Dates.now().addDay(1).date())
//                            .cell(5).writeText("蓝色单元格")
//                            .cell(6).writeText("绿色单元格")
//                            .cell(7).writeText("红色单元格")
                            // 设置第二行的红色单元格与后面一列合并，测试合并列是否会被复制
                            .merge(Range.of("H2", "I2"))

                            // 从第 2 行复制到第 3 行, 复制都是使用索引
                            .copy(Rownum.of(2).rowIndex(), Rownum.of(3).rowIndex())
                            .nextRowOfNew()
                            .cell(0).writeText("第3行：从第2行复制来的")
                            .cell(2).writeNumber(3)
                            .cell(3).writeFormula("100*B3")
                            // 将第 3 行复制，从第 6 行开始作为目标行，总共复制 5 行, 复制都是使用索引
                            .copy(Rownum.of(3).rowIndex(), Rownum.of(6).rowIndex(), 5)
                            // 切换到第5行
                            .row(Rownum.of(5))
                            .cell(0).writeText("这是第5行，第2列和第4行已隐藏").merge(Range.of("A5:E5"))

                            // 切换到第6行
                            .row(Rownum.of(6))
                            .cell(0).writeText("第6行：从第3行复制来的，清除红色单元格文字")
                            .cell(7).setCellBlank()

                            .row(Rownum.of(7))
                            .cell(0).writeText("第7行：从第3行复制来的，清除绿色单元格文字")
                            .cell(6).setCellBlank()

                            .nextRowOfNew()
                            .cell(1).appendStyle(CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.Red.color).build())
                            .setRowBlankIgnoreFromula() // 清除整行数据，公式不清除
                            .cell(0).writeText("第8行：从第3行复制来的,清除整行数据，公式不清除")

                            .nextRowOfNew()
                            .cell(1).appendStyle(CellStyles.builder().fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.Red.color).build()).writeNumber(null)
                            .setRowBlank() // 清除整行数据，公式会被清除
                            .cell(0).writeText("第9行：从第3行复制来的,清除整行数据，公式会被清除")

                            .nextRowOfNew()
                            .cell(0).writeText("第10行：从第3行复制来的")

                            .nextRowOfNew()
                            .cell(0).writeText("第11行")

                            // 隐藏第 4 行，第 2 列
                            .hideRow(Rownum.of(4).rowIndex()).hideColumn(Position.ofColumn("B").columnIndex())

//                            .autoColumnWidth()
                            .evaluateAllFormulaCells();
                    log.info(String.format("写入路径：%s",
                            sheetWriter.saveWorkBook(FPath.of("logs", "2.复制.xlsx")).absolute()
                    ));
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
                    @Cleanup final SXSSFWorkbook workbook = new SXSSFWorkbook();
                    workbook.createSheet("Sheet1");
                    workbook.getSheet("Sheet1").setDefaultColumnWidth(15);
                    SSheetWriter.of(workbook.getSheetAt(0));
                    final SSheetWriter writer = SSheetWriter
                            .of(workbook.getSheetAt(0))
                            .rowNew(Rownum.of(1)) // 从第1行开始写
                            .cellNew(0).writeNumber(100)
                            .cellNew(1).writeNumber(2)
                            .cellNew(2).writeFormulaOfRebuild("A9*B9")

//                            .autoColumnWidth()
                            .evaluateAllFormulaCells();
                    log.info(String.format("写入路径：%s",
                            writer.saveWorkBook(FPath.of("logs", "3.公式重构.xlsx")).absolute()
                    ));
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
                    @Cleanup final SXSSFWorkbook workbook = new SXSSFWorkbook();
                    workbook.createSheet("Sheet1");
                    workbook.getSheet("Sheet1").setDefaultColumnWidth(15);
                    final SSheetWriter writer = SSheetWriter.of(workbook.getSheetAt(0))
                            .setCloneStyles(Paths.get("logs", "1.普通写入.xlsx").toAbsolutePath().toString()) // 指定引用样式库文件路径
                            .rowNew(Rownum.of(1)) // 从第1行开始写
                            .cellNew(0).write(cellDatas.get(0))
                            .cellNew(1).write(cellDatas.get(1))
                            .cellNew(2).write(cellDatas.get(2))

//                            .autoColumnWidth()
                            ;

                    log.info(String.format("写入路径：%s",
                            writer.saveWorkBook(FPath.of("logs", "4.样式库引用【普通写入】.xlsx")).absolute()
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            };
            supplier.get();
        }
        { // 写入下拉选项，和批注
            Supplier supplier = () -> {
                try {
                    @Cleanup final SXSSFWorkbook workbook = new SXSSFWorkbook();
                    workbook.createSheet("Sheet1");
                    workbook.getSheet("Sheet1").setDefaultColumnWidth(15);

                    final SSheetWriter writer = SSheetWriter.of(workbook.getSheet("Sheet1"))
                            // 向 A3:A10 单元格写入下拉选项
                            .writeDropdownList(Arrays.asList("男", "女"), Range.of("A3", "A10"))
                            // 向 C3:D10 单元格写入下拉选项
                            .writeDropdownList(Arrays.asList("Java", "PHP", "Python"), Range.of("C3", "D10"))
                            // 向 E3:E10 单元格写入下拉选项，选项值来源于当前 Sheet 的 A1:C1
                            .writeDropdownList("{sheetName}!A1:C1", Range.of("E3", "E10"))

                            .rowNew(Rownum.of(1)) // 写入下拉选项的引用值
                            .cellNew(0).writeText("淘宝").writeComment("该单元格的值将会作为E列选项")
                            .cellNew(1).writeText("天猫").writeComment("该单元格的值将会作为E列选项")
                            .cellNew(2).writeText("京东").writeComment("该单元格的值将会作为E列选项")

                            .nextRowOfNew()
                            .cellNew(0).writeText("下拉选项：【男，女】")
                            .cellNew(2).writeText("下拉选项：【Java，PHP，Python】")
                            .cellNew(3).writeDropdownList(Arrays.asList("AAA", "BBB"))
                            .cellNew(4).writeDropdownList(Arrays.asList("CCC", "DDD", "EEE"))
                            .nextRowOfNew()
                            .cellNew(0).writeText("男")
                            .cellNew(2).writeText("Java")
                            .cellNew(3).writeText("Python")

                            // 向 A3:A10 单元格写入批注
                            .writeComments("只能输入【男、女】", Range.of("A3", "A10"))

                            // 冻结第 1 列 和 第 2 行
                            .freeze(1, 2)
                            // 隐藏第 1 行
                            .hideRow(Rownum.of(1).rowIndex())
                            // 锁定 A1:J1，禁止编辑
                            .lock(Range.of("A1:J1"), CellStyles.builder().locked(true).fillPattern(FillPatternType.SOLID_FOREGROUND).fillForegroundColor(Colors.Grey25Percent.color).build())
                            // 解锁 A2:J10，可以编辑
                            .unlock(Range.of("A2:J10"), CellStyles.builder().locked(false).build())
                            // 锁定功能需要设置密码才生效
                            .password("111111")
//                            .autoColumnWidth()
                            ;
                    log.info(String.format("写入路径：%s",
                            writer.saveWorkBook(FPath.of("logs", "5.下拉选项写入.xlsx")).absolute()
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            };
            supplier.get();
        }
    }
}