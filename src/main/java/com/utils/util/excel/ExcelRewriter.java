package com.utils.util.excel;

import com.utils.util.FCopy;
import com.utils.util.FPath;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * 【.xls|.xlsx】 读写同时操作
 *
 * @author Jason Xie on 2018-8-8.
 */
@Slf4j
public class ExcelRewriter implements ISheetWriter<ExcelRewriter>, ISheetReader<ExcelRewriter>, ISheetWriter.ICopyRows<ExcelRewriter>, ICellReader {
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
    public static ExcelRewriter of(@NonNull final File file, final Options ops) {
        if (!file.exists()) throw new NullPointerException("文件不存在：" + file.getAbsolutePath());
        if (file.getName().endsWith(".xls")) {
            return new ExcelRewriter(new HSSFWorkbook(new NPOIFSFileSystem(file, false)), ops);
        } else if (file.getName().endsWith(".xlsx")) {
//            @Cleanup OPCPackage pkg = OPCPackage.open(file, PackageAccess.READ_WRITE); // 这里加上 @Cleanup 会造成文件写入失败
//            return new ExcelRewriter(new XSSFWorkbook(pkg), ops);
            return new ExcelRewriter(new XSSFWorkbook(file), ops);
        } else {
            throw new IllegalArgumentException("未知的文件后缀");
        }
    }

    @Getter
    private final Options ops;
    @Getter
    private final Workbook workbook;
    private Sheet sheet;
    /**
     * 当前操作行索引
     */
    @Getter
    private int rowIndex;
    /**
     * 当前操作行
     */
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
     * @return ExcelRewriter
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
     * @return ExcelRewriter
     */
    public ExcelRewriter sheet(final String name) {
        sheet = workbook.getSheet(name);
        cell = null;
        row = null;
        rowIndex = 0;
        return this;
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
    public ExcelRewriter setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
        return this;
    }

    public ExcelRewriter row(final Row row) {
        if(Objects.nonNull(row)) rowIndex = row.getRowNum();
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

    /**
     * 获取头部列名加索引
     * 警告：重复的列名将会被覆盖；若不能保证列名不重复，请使用 {@link ExcelReader#headers()}
     *
     * @return Map<String, Integer>
     */
    public LinkedHashMap<String, Integer> mapHeaders() {
        final LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            map.put(cell(i).stringOfEmpty().trim(), i);
        }
        map.remove("");
        return map;
    }

    @SneakyThrows
    public static void main(String[] args) {
        Paths.get("logs").toFile().mkdir();
        {
            ExcelRewriter rwriter = ExcelRewriter
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
                    .row(1);
            rwriter
                    .cell(0).writeNumber(1)
                    .cell(2).writeByCellType("187-0000-0000")

                    .next()
                    .cell(0).writeNumber(2)
                    .cell(2).writeByCellType("187-0000-0001")

                    .next()
                    .cell(0).writeNumber(3)
                    .cell(2).writeByCellType("187-0000-0002")
            ;

            Path path = Paths.get("logs", "重写.xlsx").toAbsolutePath();
            @Cleanup FileOutputStream fileOutputStream = new FileOutputStream(path.toFile());
            rwriter.getWorkbook().write(fileOutputStream);
            log.debug("写入路径：" + path.toAbsolutePath());
        }
        {
            ExcelRewriter rwriter = ExcelRewriter
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
                    .row(1);
            rwriter
                    .cell(0).writeNumber(1)
                    .cell(2).writeByCellType("187-0000-0000")

                    .next()
                    .cell(0).writeNumber(2)
                    .cell(2).writeByCellType("187-0000-0001")

                    .next()
                    .cell(0).writeNumber(3)
                    .cell(2).writeByCellType("187-0000-0002")
            ;

            Path path = Paths.get("logs", "重写.xls").toAbsolutePath();
            @Cleanup FileOutputStream fileOutputStream = new FileOutputStream(path.toFile());
            rwriter.getWorkbook().write(fileOutputStream);
            log.debug("写入路径：" + path.toAbsolutePath());
        }
    }
}