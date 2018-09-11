package com.utils.util.excel;

import com.utils.common.entity.excel.Header;
import com.utils.common.entity.excel.Position;
import com.utils.enums.DataType;
import com.utils.exception.NotFoundException;
import com.utils.util.FPath;
import com.utils.util.FWrite;
import com.utils.util.Util;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

/**
 * 【.xsl|.xslx】 文件读取
 *
 * @author Jason Xie on 2018-8-8.
 */
@Slf4j
public class ExcelReader implements ISheetReader<ExcelReader>, ICellReader {
    private ExcelReader(final Workbook workbook) {
        this.workbook = workbook;
        this.dataFormatter = new DataFormatter();
    }

    public static ExcelReader of(final String path, String... names) {
        return of(FPath.of(path, names).file(), true);
    }

    public static ExcelReader of(@NonNull final File file) {
        return of(file, true);
    }

    @SneakyThrows
    public static ExcelReader of(@NonNull final File file, final boolean readOnly) {
        if (!file.exists()) throw new NotFoundException("文件不存在：".concat(file.getAbsolutePath()));
        if (file.getName().endsWith(".xls")) {
            return new ExcelReader(new HSSFWorkbook(new NPOIFSFileSystem(file, readOnly)));
        } else if (file.getName().endsWith(".xlsx")) {
            @Cleanup final OPCPackage pkg = OPCPackage.open(file, readOnly ? PackageAccess.READ : PackageAccess.READ_WRITE);
            return new ExcelReader(new XSSFWorkbook(pkg));
        } else {
            throw new IllegalArgumentException("未知的文件后缀");
        }
    }

    public static ExcelReader of(@NonNull final Sheet sheet) {
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

    public DataFormatter getDataFormatter() {
        return dataFormatter;
    }

    /**
     * 按索引选择读取sheet
     *
     * @param index int sheet索引
     * @return ExcelReader
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
     * @return ExcelReader
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
        if (Objects.nonNull(row)) rowIndex = row.getRowNum();
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
     * 获取头部列名加索引
     *
     * @return {@link Header}
     */
    public List<Header> headers() {
        final List<Header> headers = new ArrayList<>();
        String label;
        for (int i = 0; i < row.getLastCellNum(); i++) {
            cell(i);
            if (Util.isNotEmpty(label = stringValue()))
                headers.add(Header.builder().index(i).label(label.trim()).type(DataType.TEXT).sindex(sindex()).build());
        }
        return headers;
    }

    /**
     * 获取头部列名加索引
     * 警告：重复的列名将会被覆盖；若不能保证列名不重复，请使用 {@link ExcelReader#headers()}
     *
     * @return Map<String               ,                               Integer>
     */
    public LinkedHashMap<String, Integer> mapHeaders() {
        final LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            map.put(cell(i).stringOfEmpty().trim(), i);
        }
        map.remove("");
        return map;
    }

    /**
     * 获取当前行，整行数据
     *
     * @return LinkedHashMap<Integer               ,                               Object>
     */
    public LinkedHashMap<Integer, Object> rowObject() {
        final LinkedHashMap<Integer, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            map.put(i, cell(i).value().orElse(null));
        }
        return map;
    }

    /**
     * 获取当前行指定列数据
     *
     * @param headers List<Header> 来自 {@link ExcelReader#headers()}
     * @return LinkedHashMap<String                                                                                                                               ,                                                                                                                                                                                                                                                               Object>
     */
    public LinkedHashMap<String, Object> rowObject(final List<Header> headers) {
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        headers.forEach(header -> map.put(header.getLabel(), cell(header.getIndex()).value()));
        return map;
    }

    /**
     * 获取当前行指定列数据
     *
     * @param mapHeaders Map<String, Integer> 来自 {@link ExcelReader#mapHeaders()}
     * @return LinkedHashMap<String                                                                                                                               ,                                                                                                                                                                                                                                                               Object>
     */
    public LinkedHashMap<String, Object> rowObject(final Map<String, Integer> mapHeaders) {
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        mapHeaders.forEach((key, value) -> map.put(key, cell(value).value().orElse(null)));
        return map;
    }

    @SneakyThrows
    public void close() {
        workbook.close();
    }

    public static void main(String[] args) {
        {
            Consumer<File> read = (file) -> {
                final List<Integer> columnIndexs = Arrays.asList(
                        Position.ofColumn("A").columnIndex(),
                        Position.ofColumn("B").columnIndex(),
                        Position.ofColumn("C").columnIndex(),
                        Position.ofColumn("D").columnIndex(),
                        Position.ofColumn("E").columnIndex(),
                        Position.ofColumn("F").columnIndex()
                );
                StringBuffer sb = new StringBuffer();
                @Cleanup ExcelReader reader = ExcelReader.of(file).sheet(0).row(1); // 读取第 1 个 sheet ，从第 2 行开始
                do {
                    sb.append("<tr>\n");
                    columnIndexs.forEach(index ->
                            sb.append("<td class=\"text-x-small p-3" + (index == 0 ? " text-left" : "") + "\">")
                                    .append(reader.cell(index).stringOfEmpty())
                                    .append("</td>\n")
                    );
                    sb.append("</tr>\n");
                } while (Objects.nonNull(reader.next()));
                System.out.println(FWrite.of(String.format("src/test/files/temp/%s.html", file.getName())).write(sb.toString()).getAbsolute().orElse(null));
            };
            read.accept(FPath.of("src/test/files/excel/test.xls").file());
            read.accept(FPath.of("src/test/files/excel/test.xlsx").file());
        }
        {
            Consumer<File> read = (file) -> {
                final int A = Position.ofColumn("A").columnIndex();
                final int B = Position.ofColumn("B").columnIndex();
                final int C = Position.ofColumn("C").columnIndex();
                @Cleanup ExcelReader reader = ExcelReader.of(file).sheet(3).row(1); // 读取第 4 个 sheet ，从第 2 行开始
                Map<String, TreeSet<String>> map = new LinkedHashMap<>();
                do {
                    map.put(reader.cell(A).stringOfEmpty(), new TreeSet<>());
                } while (Objects.nonNull(reader.next()));
                reader.row(1);
                do {
                    map.get(reader.cell(A).stringOfEmpty())
                            .add(Objects.isNull(reader.cell(B).stringValue()) ? reader.cell(C).stringValue() : reader.cell(B).stringValue());
                } while (Objects.nonNull(reader.next()));

                System.out.println(FWrite.of(String.format("src/test/files/temp/%s.json", file.getName())).writeJson(map).getAbsolute().orElse(null));
            };
            read.accept(FPath.of("src/test/files/excel/地区划分.xls").file());
            read.accept(FPath.of("src/test/files/excel/地区划分.xlsx").file());
        }
    }
}
