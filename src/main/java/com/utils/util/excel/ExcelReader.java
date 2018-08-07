package com.utils.util.excel;

import com.utils.common.entity.excel.Header;
import com.utils.common.entity.excel.Position;
import com.utils.common.entity.excel.Rownum;
import com.utils.enums.DataType;
import com.utils.util.*;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
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
 * .xslx 文件读取
 *
 * @author Jason Xie on 2017/11/28.
 */

@Slf4j
public class ExcelReader implements ICellReader {
    private ExcelReader(final Workbook workbook) {
        this.workbook = workbook;
    }

    public static ExcelReader of(final String path, String... names) {
        return of(FPath.of(path, names).file());
    }

    public static ExcelReader of(@NonNull final File file) {
        return of(file, true);
    }

    @SneakyThrows
    public static ExcelReader of(@NonNull final File file, final boolean readOnly) {
        if (!file.exists()) throw new NullPointerException("文件不存在：" + file.getAbsolutePath());
        if (file.getName().endsWith(".xls")) {
            return new ExcelReader(new HSSFWorkbook(new NPOIFSFileSystem(file, readOnly)));
        } else if (file.getName().endsWith(".xlsx")) {
            @Cleanup OPCPackage pkg = OPCPackage.open(file, readOnly ? PackageAccess.READ : PackageAccess.READ_WRITE);
            return new ExcelReader(new XSSFWorkbook(pkg));
        } else {
            throw new IllegalArgumentException("未知的文件后缀");
        }
    }
    public static ExcelReader of(@NonNull final Sheet sheet) {
        ExcelReader reader = new ExcelReader(sheet.getWorkbook());
        reader.sheet = sheet;
        return reader;
    }

    final private Workbook workbook;
    /**
     * 当前操作sheet
     */
    @Getter
    private Sheet sheet;
    /**
     * 当前操作行
     */
    private Row row;
    /**
     * 当前操作列
     */
    @Getter
    private Cell cell;
    /**
     * 当前行索引
     */
    @Getter
    private int rowIndex;
    /**
     * 最后一行索引
     */
    @Getter
    private int lastRowIndex;
    private DataFormatter dataFormatter;

    public DataFormatter getDataFormatter() {
        if (Objects.isNull(dataFormatter)) dataFormatter = new DataFormatter();
        return dataFormatter;
    }

    /**
     * 按索引选择读取sheet
     * @param index int sheet索引
     * @return ExcelReader
     */
    public ExcelReader sheet(final int index) {
        sheet = workbook.getSheetAt(index);
        lastRowIndex = sheet.getLastRowNum();
        return this;
    }

    /**
     * 按名称选择读取sheet
     * @param name String sheet名称
     * @return ExcelReader
     */
    public ExcelReader sheet(final String name) {
        sheet = workbook.getSheet(name);
        lastRowIndex = sheet.getLastRowNum();
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

    /**
     * 选择读取行
     * @param rowIndex int 行索引
     * @return ExcelReader
     */
    public ExcelReader row(final int rowIndex) {
        this.rowIndex = rowIndex;
        this.row = sheet.getRow(rowIndex);
        this.cell = null;
        return this;
    }

    /**
     * 选择读取行
     *
     * @param rownum Rownum 数据行
     * @return ExcelReader
     */
    public ExcelReader row(@NonNull final Rownum rownum) {
        row(rownum.rowIndex());
        return this;
    }

    /**
     * 换行
     *
     * @return ExcelReader
     */
    public ExcelReader next() {
        if (!hasEnd()) {
            row(rowIndex + 1);
            if (Objects.isNull(this.row)) next();
        }
        return this;
    }
    /**
     * 删除行，保留空行
     *
     * @return ExcelReader
     */
    public ExcelReader clearRow() {
        sheet.removeRow(row);
        return this;
    }
    /**
     * 删除行，整行上移
     *
     * @return ExcelReader
     */
    public ExcelReader deleteRow() {
        sheet.shiftRows(rowIndex, rowIndex, 1);
        return this;
    }
    /**
     * 选择读取列
     * @param columnIndex int 列索引
     * @return ExcelReader
     */
    public ExcelReader cell(final int columnIndex) {
        this.cell = Objects.isNull(row) ? null : row.getCell(columnIndex);
        return this;
    }

    /**
     * 获取头部列名加索引
     * @return {@link Header}
     */
    public List<Header> headers() {
        final List<Header> headers = new ArrayList<>();
        String label;
        for (int i = 0; i < row.getLastCellNum(); i++) {
            cell(i);
            if (Util.isNotEmpty(label = text()))
                headers.add(Header.builder().index(i).label(label.trim()).type(DataType.TEXT).sindex(sindex()).build());
        }
        return headers;
    }

    /**
     * 获取头部列名加索引
     * 警告：重复的列名将会被覆盖；若不能保证列名不重复，请使用 {@link ExcelReader#headers()}
     * @return Map<String, Integer>
     */
    public LinkedHashMap<String, Integer> mapHeaders() {
        final LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            map.put(cell(i).textOfEmpty().trim(), i);
        }
        map.remove("");
        return map;
    }

    /**
     * 获取当前行，整行数据
     * @return LinkedHashMap<Integer, Object>
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
     * @param headers List<Header> 来自 {@link ExcelReader#headers()}
     * @return LinkedHashMap<String, Object>
     */
    public LinkedHashMap<String, Object> rowObject(final List<Header> headers) {
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        headers.forEach(header -> map.put(header.getLabel(), cell(header.getIndex()).value()));
        return map;
    }
    /**
     * 获取当前行指定列数据
     * @param mapHeaders Map<String, Integer> 来自 {@link ExcelReader#mapHeaders()}
     * @return LinkedHashMap<String, Object>
     */
    public LinkedHashMap<String, Object> rowObject(final Map<String, Integer> mapHeaders) {
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        mapHeaders.forEach((key, value) -> map.put(key, cell(value).value().orElse(null)));
        return map;
    }

    /**
     * 数据是否已读完
     * @return boolean true：最后一行已经读完
     */
    public boolean hasEnd() {
        return rowIndex > lastRowIndex;
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
                                    .append(reader.cell(index).textOfEmpty())
                                    .append("</td>\n")
                    );
                    sb.append("</tr>\n");
                } while (!reader.next().hasEnd());
                System.out.println(FWrite.of("", "src", "test", "files", "temp",file.getName() + ".html").write(sb.toString()).getAbsolute().orElse(null));
            };
            read.accept(FPath.of("", "src", "test", "files", "excel", "test.xls").file());
            read.accept(FPath.of("", "src", "test", "files", "excel", "test.xlsx").file());
        }
        {
            Consumer<File> read = (file) -> {
                final int A = Position.ofColumn("A").columnIndex();
                final int B = Position.ofColumn("B").columnIndex();
                final int C = Position.ofColumn("C").columnIndex();
                @Cleanup ExcelReader reader = ExcelReader.of(file).sheet(3).row(1); // 读取第 4 个 sheet ，从第 2 行开始
                Map<String, TreeSet<String>> map = new LinkedHashMap<>();
                do {
                    map.put(reader.cell(A).textOfEmpty(), new TreeSet<>());
                } while (!reader.next().hasEnd());
                reader.row(1);
                do {
                    map.get(reader.cell(A).textOfEmpty())
                            .add(Objects.isNull(reader.cell(B).text()) ? reader.cell(C).text() : reader.cell(B).text());
                } while (!reader.next().hasEnd());

                System.out.println(FWrite.of("", "src", "test", "files", "temp",file.getName() + ".json").writeJson(map).getAbsolute().orElse(null));
            };
            read.accept(FPath.of("", "src", "test", "files", "excel", "地区划分.xls").file());
            read.accept(FPath.of("", "src", "test", "files", "excel", "地区划分.xlsx").file());
        }
    }
}
