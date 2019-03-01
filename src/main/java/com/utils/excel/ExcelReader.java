package com.utils.excel;

import com.utils.excel.entity.Header;
import com.utils.excel.entity.Position;
import com.utils.exception.NotFoundException;
import com.utils.util.FPath;
import com.utils.util.FWrite;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

/**
 * 【.xsl|.xslx】 文件读取
 *
 * @author 谢长春 on 2018-8-8 .
 */
@Slf4j
public final class ExcelReader implements ISheetReader<ExcelReader> {
    private ExcelReader(final Workbook workbook) {
        this.workbook = workbook;
        this.dataFormatter = new DataFormatter();
    }

    public static ExcelReader of(final String path, String... names) {
        return of(FPath.of(path, names).file(), true);
    }

    public static ExcelReader of(final File file) {
        Objects.requireNonNull(file, "参数【file】是必须的");
        return of(file, true);
    }

    @SneakyThrows
    public static ExcelReader of(final File file, final boolean readOnly) {
        Objects.requireNonNull(file, "参数【file】是必须的");
        if (!file.exists()) {
            throw new NotFoundException("文件不存在：".concat(file.getAbsolutePath()));
        }
        if (file.getName().endsWith(".xls")) {
            return new ExcelReader(new HSSFWorkbook(new POIFSFileSystem(file, readOnly)));
        } else if (file.getName().endsWith(".xlsx")) {
            @Cleanup final OPCPackage pkg = OPCPackage.open(file, readOnly ? PackageAccess.READ : PackageAccess.READ_WRITE);
            return new ExcelReader(new XSSFWorkbook(pkg));
        } else {
            throw new IllegalArgumentException("未知的文件后缀");
        }
    }

    @SneakyThrows
    public static ExcelReader of(final File file, final boolean readOnly, final String password) {
        Objects.requireNonNull(file, "参数【file】是必须的");
        if (!file.exists()) {
            throw new NotFoundException("文件不存在：".concat(file.getAbsolutePath()));
        }
        if (!file.getName().matches("[\\s\\S]+\\.xls(x)?$")) {
            throw new IllegalArgumentException("未知的文件后缀");
        }
        return new ExcelReader(WorkbookFactory.create(file, password, readOnly));
    }

    public static ExcelReader of(final Sheet sheet) {
        Objects.requireNonNull(sheet, "参数【sheet】是必须的");
        final ExcelReader reader = new ExcelReader(sheet.getWorkbook());
        reader.sheet = sheet;
        return reader;
    }

    @Getter
    final private Workbook workbook;
    /**
     * 当前操作sheet
     */
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
    @Setter
    private Cell cell;
    private DataFormatter dataFormatter;

    @Override
    public DataFormatter getDataFormatter() {
        return dataFormatter;
    }

    /**
     * 按索引选择读取sheet
     *
     * @param index int sheet索引
     * @return {@link ExcelReader}
     */
    public ExcelReader sheet(final int index) {
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
     * @return {@link ExcelReader}
     */
    public ExcelReader sheet(final String name) {
        sheet = workbook.getSheet(name);
        cell = null;
        row = null;
        rowIndex = 0;
//        sheet.getMergedRegions().forEach(address -> {
//            int row = address.getFirstRow();
//            int cell = address.getFirstColumn();
//            for (int r = address.getFirstRow(); r < address.getLastRow(); r++) {
//                for (int c = address.getFirstColumn(); c < address.getLastRow(); c++) {
//
//                }
//            }
//        });
        return this;
    }

    @Override
    public ExcelReader setRowIndex(final int rowIndex) {
        this.rowIndex = rowIndex;
        return this;
    }

    @Override
    public ExcelReader row(final Row row) {
        if (Objects.nonNull(row)) {
            rowIndex = row.getRowNum();
        }
        this.row = row;
        this.cell = null;
        return this;
    }

    @Override
    public ExcelReader cell(final Cell cell) {
        this.cell = cell;
        return this;
    }

    /**
     * 获取当前行指定列数据
     *
     * @param headers {@link List}{@link List<Header>} 来自 {@link ExcelReader#headers()}
     * @return {@link com.utils.excel.entity.Row}{@link com.utils.excel.entity.Row<int:Header对象中的index字段值, String:单元格内容>}
     */
    public com.utils.excel.entity.Row rowObject(final List<Header> headers) {
//        final LinkedHashMap<String, String> map = new LinkedHashMap<>();
        final com.utils.excel.entity.Row row = com.utils.excel.entity.Row.build();
        headers.forEach(header -> row.addCell(header.getIndex(),
                com.utils.excel.entity.Cell.builder()
                        .text(cell(header.getIndex()).stringValue())
//                        .type()
//                        .value()
//                        .formula()
//                        .sindex()
                        .build()
        ));
        return row;
    }

    /**
     * 获取当前行指定列数据
     *
     * @param mapHeaders {@link Map}{@link Map<String:列头字段名, Integer:列索引>} 来自 {@link ExcelReader#mapHeaders()}
     * @return {@link LinkedHashMap}{@link LinkedHashMap<String:Header对象中的label字段值, String:单元格内容>}
     */
    public LinkedHashMap<String, String> rowObject(final Map<String, Integer> mapHeaders) {
        final LinkedHashMap<String, String> map = new LinkedHashMap<>();
        mapHeaders.forEach((key, value) -> map.put(key, cell(value).stringValue()));
        return map;
    }

    /**
     * 获取当前行，整行数据
     *
     * @return {@link LinkedHashMap}{@link LinkedHashMap<Integer:列索引, String:单元格内容>}
     */
    public LinkedHashMap<Integer, String> rowObject() {
        final LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
        for (int i = 0; i < getRow().getLastCellNum(); i++) {
            map.put(i, cell(i).stringValue());
        }
        return map;
    }

    public static void main(String[] args) {
        {
            final Consumer<File> read = (file) -> {
                final List<Integer> columnIndexs = Arrays.asList(
                        Position.ofColumn("A").columnIndex(),
                        Position.ofColumn("B").columnIndex(),
                        Position.ofColumn("C").columnIndex(),
                        Position.ofColumn("D").columnIndex(),
                        Position.ofColumn("E").columnIndex(),
                        Position.ofColumn("F").columnIndex()
                );
                final StringBuffer sb = new StringBuffer();
                @Cleanup final ExcelReader reader = ExcelReader.of(file).sheet(0) // 读取第 1 个 sheet
                        .skip(1); // 跳过第 1 行从第 2 行开始
                while (reader.hasNext()) {
                    sb.append("<tr>\n");
                    columnIndexs.forEach(index ->
                            sb.append("<td class=\"text-x-small p-3" + (index == 0 ? " text-left" : "") + "\">")
                                    .append(reader.cell(index).stringOfEmpty())
                                    .append("</td>\n")
                    );
                    sb.append("</tr>\n");
                }
                System.out.println(FWrite.of(String.format("src/test/files/temp/%s.html", file.getName())).write(sb.toString()).getAbsolute().orElse(null));
            };
            read.accept(FPath.of("src/test/files/excel/test.xls").file());
            read.accept(FPath.of("src/test/files/excel/test.xlsx").file());
        }
        {
            final Consumer<File> read = (file) -> {
                final int A = Position.ofColumn("A").columnIndex();
                final int B = Position.ofColumn("B").columnIndex();
                final int C = Position.ofColumn("C").columnIndex();
                @Cleanup final ExcelReader reader = ExcelReader.of(file).sheet(3) // 读取第 4 个 sheet
                        .skip(1); // 跳过第 1 行从第 2 行开始
                final Map<String, TreeSet<String>> map = new LinkedHashMap<>();
                while (reader.hasNext()) {
                    map.put(reader.cell(A).stringOfEmpty(), new TreeSet<>());
                }
                reader.row(Rownum.of(2)); // 重新定位到第 2 行
                do {
                    map.get(reader.cell(A).stringOfEmpty())
                            .add(Objects.isNull(reader.cell(B).stringValue()) ? reader.cell(C).stringValue() : reader.cell(B).stringValue());
                } while (reader.hasNext());

                System.out.println(FWrite.of(String.format("src/test/files/temp/%s.json", file.getName())).writeJson(map).getAbsolute().orElse(null));
            };
            read.accept(FPath.of("src/test/files/excel/地区划分.xls").file());
            read.accept(FPath.of("src/test/files/excel/地区划分.xlsx").file());
        }
        {
            final Consumer<File> read = (file) -> {
                @Cleanup final ExcelReader reader = ExcelReader.of(file).sheet(0); // 读取第 1 个 sheet
//                @Cleanup final ExcelReader reader = ExcelReader.of(file, true, "111111").sheet(0); // 读取第 1 个 sheet
                final List<Header> header = reader.row(Rownum.of(1)).headers();
                final List<com.utils.excel.entity.Row> body = new ArrayList<>();
                while (reader.hasNext()) {
                    body.add(reader.rowObject(header));
                }
                System.out.println(FWrite.of(String.format("src/test/files/temp/%s.json", file.getName()))
                        .writeJson(
                                com.utils.excel.entity.Table.builder()
                                        .header(header)
                                        .body(body)
                                        .build()
                        )
                        .getAbsolute()
                        .orElse(null)
                );
            };
            read.accept(FPath.of("src/test/files/excel/联系人-111111.xls").file());
            read.accept(FPath.of("src/test/files/excel/联系人-111111.xlsx").file());
        }
    }
}
