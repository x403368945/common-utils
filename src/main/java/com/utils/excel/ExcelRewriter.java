package com.utils.excel;

import com.utils.excel.enums.Column;
import com.utils.exception.NotFoundException;
import com.utils.util.Dates;
import com.utils.util.FCopy;
import com.utils.util.FPath;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * 【.xls|.xlsx】 读写同时操作；写入完成之后需要调用 close 方法；否则可能造成内存泄露
 *
 * @author 谢长春 on 2018-8-8 .
 */
@Slf4j
public final class ExcelRewriter implements ISheetWriter<ExcelRewriter>, ISheetReader<ExcelRewriter>, ISheetWriter.ICopyRows<ExcelRewriter> {
    private ExcelRewriter(final Workbook workbook, final Options ops) {
        this.ops = Objects.isNull(ops) ? Options.builder().build() : ops;
        this.workbook = workbook;
        this.dataFormatter = new DataFormatter();
//        this.sheet.setForceFormulaRecalculation(true); // 设置强制刷新公式
        // 若此上面一行设置不起作用，则在写入文件之前使用这行代码强制刷新公式：FormulaEvaluator.evaluateAllFormulaCells(workbook);
    }

    public static ExcelRewriter of(final String path, String... names) {
        return of(FPath.of(path, names).file(), null);
    }

    @SneakyThrows
    public static ExcelRewriter of(final File file, final Options ops) {
//        Objects.requireNonNull(file, "参数【file】是必须的");
//        if (!file.exists()) throw new NullPointerException("文件不存在：" + file.getAbsolutePath());
//        if (file.getName().endsWith(".xls")) {
//            return new ExcelRewriter(new HSSFWorkbook(new NPOIFSFileSystem(file, false)), ops);
//        } else if (file.getName().endsWith(".xlsx")) {
////            @Cleanup OPCPackage pkg = OPCPackage.open(file, PackageAccess.READ_WRITE); // 这里加上 @Cleanup 会造成文件写入失败
////            return new ExcelRewriter(new XSSFWorkbook(pkg), ops);
//            return new ExcelRewriter(new XSSFWorkbook(file), ops);
//        } else {
//            throw new IllegalArgumentException("未知的文件后缀");
//        }
        return of(file, ops, null);
    }

    @SneakyThrows
    public static ExcelRewriter of(final File file, final Options ops, final String password) {
        Objects.requireNonNull(file, "参数【file】是必须的");
        if (!file.exists()) {
            throw new NotFoundException("文件不存在：".concat(file.getAbsolutePath()));
        }
        if (!file.getName().matches("[\\s\\S]*\\.xls(x)?$")) {
            throw new IllegalArgumentException("未知的文件后缀");
        }
//        return new ExcelRewriter(WorkbookFactory.create(file, password, false), ops);
        // 用 new FileInputStream(file) 初始化可以防止篡改模板
        return new ExcelRewriter(WorkbookFactory.create(new FileInputStream(file), password), ops);
    }

    @Getter
    private final Options ops;
    @Getter
    private final Workbook workbook;
    @Getter
    private Sheet sheet;
    /**
     * 当前操作行索引
     */
    @Getter
    private int rowIndex;
    /**
     * 当前操作行
     */
    @Getter
    private Row row;
    /**
     * 当前操作单元格
     */
    @Getter
    private Cell cell;
    @Getter
    private DataFormatter dataFormatter;

    /**
     * 按索引选择读取sheet
     *
     * @param index int sheet索引
     * @return {@link ExcelRewriter}
     */
    public ExcelRewriter sheet(final int index) {
        sheet = workbook.getSheetAt(index);
        cell = null;
        row = null;
        rowIndex = 0;
        return this;
    }

    /**
     * 按名称选择读取sheet
     *
     * @param name String sheet名称
     * @return {@link ExcelRewriter}
     */
    public ExcelRewriter sheet(final String name) {
        sheet = workbook.getSheet(name);
        cell = null;
        row = null;
        rowIndex = 0;
        return this;
    }

    @Override
    public ExcelRewriter setRowIndex(final int rowIndex) {
        this.rowIndex = rowIndex;
        return this;
    }

    @Override
    public ExcelRewriter row(final Row row) {
        if (Objects.nonNull(row)) {
            rowIndex = row.getRowNum();
        }
        this.row = row;
        this.cell = null; // 切换行，需要将 cell 置空
        return this;
    }

    @Override
    public ExcelRewriter cell(final Cell cell) {
        this.cell = cell;
        return this;
    }

    @Override
    public ExcelRewriter copyTo(int toRowIndex) {
        copy(rowIndex, toRowIndex);
        return this;
    }

    @SneakyThrows
    public static void main(String[] args) {
        Paths.get("logs").toFile().mkdir();
        {
            @Cleanup final ExcelRewriter rwriter = ExcelRewriter
                    .of(
                            FCopy.ofDefault()
                                    .from("src/test/files/excel/联系人.xlsx")
                                    .rename().to("src/test/files/temp/")
                                    .copy()
                                    .getNewFile()
                                    .orElseThrow(() -> new RuntimeException("文件复制失败")),
                            Options.builder().build()
                    )
                    .sheet(0)
                    .row(Rownum.of(2)) // 选定第二行
                    .cell(0).writeNumber(1)
                    .cell(2).writeByCellType("187-0000-0000")

                    .next()
                    .cell(0).writeNumber(2)
                    .cell(2).writeByCellType("187-0000-0001")

                    .next()
                    .cell(0).writeNumber(3)
                    .cell(2).writeByCellType("187-0000-0002");
            log.info(String.format("写入路径：%s", rwriter.saveWorkBook(FPath.of("logs", "联系人-重写.xlsx")).absolute()));
        }
        {
            @Cleanup final ExcelRewriter rwriter = ExcelRewriter
                    .of(
                            FCopy.ofDefault()
                                    .from("src/test/files/excel/联系人.xls")
                                    .rename().to("src/test/files/temp/")
                                    .copy()
                                    .getNewFile()
                                    .orElseThrow(() -> new RuntimeException("文件复制失败")),
                            Options.builder().build()
                    )
                    .sheet(0)
                    .row(Rownum.of(2)) // 选定第二行
                    .cell(0).writeNumber(1)
                    .cell(2).writeByCellType("187-0000-0000")

                    .next()
                    .cell(0).writeNumber(2)
                    .cell(2).writeByCellType("187-0000-0001")

                    .next()
                    .cell(0).writeNumber(3)
                    .cell(2).writeByCellType("187-0000-0002");
            log.info(String.format("写入路径：%s", rwriter.saveWorkBook(FPath.of("logs", "联系人-重写.xls")).absolute()));
        }
        {
            @Cleanup final ExcelRewriter rwriter = ExcelRewriter
                    .of(
                            FCopy.ofDefault()
                                    .from("src/test/files/excel/联系人-111111.xlsx")
                                    .rename().to("src/test/files/temp/")
                                    .copy()
                                    .getNewFile()
                                    .orElseThrow(() -> new RuntimeException("文件复制失败")),
                            Options.builder().build()
//                            , "111111"
                    )
                    .sheet(0)
                    .row(Rownum.of(2)) // 选定第二行
                    .cell(0).writeNumber(1)
                    .cell(2).writeByCellType("187-0000-0000")
                    .cellOfNew(4).writeDate(Dates.now().timestamp())

                    .next()
                    .cell(0).writeNumber(2)
                    .cell(2).writeByCellType("187-0000-0001")
                    .cellOfNew(4).writeDate(Dates.now().timestamp())

                    .next()
                    .cell(0).writeNumber(3)
                    .cell(2).writeByCellType("187-0000-0002")
                    .cellOfNew(4).writeDate(Dates.now().timestamp())

                    .nextRowOfNew()
                    .cellOfNew(0).writeNumber(4)
                    .cellOfNew(1).writeText("Joe")
                    .cellOfNew(2).writeText("187-0000-0003")
                    .cellOfNew(3).writeText("Joe@gmail.com")
                    .cellOfNew(4).writeDate(Dates.now().timestamp());
            log.info(String.format("写入路径：%s", rwriter.saveWorkBook(FPath.of("logs", "联系人-111111-重写.xlsx")).absolute()));
        }
        {
            @Cleanup final ExcelRewriter rwriter = ExcelRewriter
                    .of(
                            FCopy.ofDefault()
                                    .from("src/test/files/excel/联系人-111111.xls")
                                    .rename().to("src/test/files/temp/")
                                    .copy()
                                    .getNewFile()
                                    .orElseThrow(() -> new RuntimeException("文件复制失败")),
                            Options.builder().build()
//                            , "111111"
                    )
                    .sheet(0)
                    .row(Rownum.of(2)) // 选定第二行
                    .cell(0).writeNumber(1)
                    .cell(2).writeByCellType("187-0000-0000")
                    .cellOfNew(4).writeDate(Dates.now().timestamp())

                    .next()
                    .cell(0).writeNumber(2)
                    .cell(2).writeByCellType("187-0000-0001")
                    .cellOfNew(4).writeDate(Dates.now().timestamp())

                    .next()
                    .cell(0).writeNumber(3)
                    .cell(2).writeByCellType("187-0000-0002")
                    .cellOfNew(4).writeDate(Dates.now().timestamp())

                    .nextRowOfNew()
                    .cellOfNew(0).writeNumber(4)
                    .cellOfNew(1).writeText("Joe")
                    .cellOfNew(2).writeText("187-0000-0003")
                    .cellOfNew(3).writeText("Joe@gmail.com")
                    .cellOfNew(4).writeDate(Dates.now().timestamp());
            log.info(String.format("写入路径：%s", rwriter.saveWorkBook(FPath.of("logs", "联系人-111111-重写.xls")).absolute()));
        }
        {
            @Cleanup final ExcelRewriter rwriter = ExcelRewriter
                    .of(
                            FPath.of("src/test/files/excel/模板文件.xlsx").file(),
                            Options.builder().build()
//                            , "111111"
                    )
                    .sheet(0)
                    .rowNew(Rownum.of(2)) // 选定第二行
                    .cellNew(Column.A).writeNumber(1)
                    .cellNew(Column.B).writeText("187-0000-0000")

                    .nextRowNew()
                    .cellNew(Column.A).writeNumber(2)
                    .cellNew(Column.B).writeText("187-0000-0001");
            log.info(String.format("写入路径：%s", rwriter.saveWorkBook(FPath.of("logs", "模板文件-重写.xlsx")).absolute()));
        }

    }
}