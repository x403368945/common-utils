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
public class ExcelReader {
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

    private DataFormatter getDataFormatter() {
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
                headers.add(Header.builder().index(i).label(label.trim()).type(DataType.Text).sindex(sindex()).build());
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

    /**
     * 判断单元格是否非空
     * @return boolean true：非空，false：空
     */
    public boolean cellNotEmpty() {
        return Objects.nonNull(this.cell);
    }

    /**
     * 判断单元格是否为空
     * @return boolean true：空，false：非空
     */
    private boolean cellEmpty() {
        return Objects.isNull(cell) || Objects.equals(CellType.BLANK, cell.getCellTypeEnum());
    }

    /**
     * 返回单元格数据原始值
     * @return Optional<Object>
     */
    public Optional<Object> value() {
        return value(false);
    }

    /**
     * 获取单元格数据
     * @param format boolean 返回时是否格式化单元格数据；true：是，false：否
     * @return Optional<Object>
     */
    public Optional<Object> value(final boolean format) {
        if (cellEmpty()) return Optional.empty();
        switch (cell.getCellTypeEnum()) {
            case STRING:
                return Optional.of(cell.getStringCellValue());
            case NUMERIC:
                if (format) {
                    String formatPattern = cell.getCellStyle().getDataFormatString();
                    final CellStyle style = cell.getCellStyle();
                    if (Util.isEmpty(formatPattern)) {
                        formatPattern = BuiltinFormats.getBuiltinFormat(style.getDataFormat());
                    }
                    return Optional.of(getDataFormatter().formatRawCellContents(cell.getNumericCellValue(), style.getDataFormat(), formatPattern));
                }
                return Optional.of(DateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue().getTime() : cell.getNumericCellValue());
            case BOOLEAN:
                return Optional.of(cell.getBooleanCellValue());
            case FORMULA:
                if (format) {
                    String formatPattern = cell.getCellStyle().getDataFormatString();
                    final CellStyle style = cell.getCellStyle();
                    if (Util.isEmpty(formatPattern)) {
                        formatPattern = BuiltinFormats.getBuiltinFormat(style.getDataFormat());
                    }
                    return Optional.of(new DataFormatter().formatRawCellContents(cell.getNumericCellValue(), style.getDataFormat(), formatPattern));
                }
                switch (cell.getCachedFormulaResultTypeEnum()) {
                    case _NONE:
                        break;
                    case NUMERIC:
                        return Optional.of(cell.getNumericCellValue());
                    case STRING:
                        return Optional.of(cell.getStringCellValue());
                    case FORMULA:
                        break;
                    case BLANK:
                        break;
                    case BOOLEAN:
                        break;
                    case ERROR:
                        break;
                }
        }
        cell.setCellType(CellType.STRING);
        return Optional.of(cell.getStringCellValue());
    }

    /**
     * 获取单元格文本，保留null值
     * @return String
     */
    public String text() {
        return value().map(v -> {
            if (v instanceof Double) return Num.of(v).toBigDecimal().toPlainString(); // 解决科学计数法 toString()问题
            else return v.toString();
        }).orElse(null);
    }

    /**
     * 获取单元格文本，null值默认为空字符串 ""
     * @return String
     */
    public String textOfEmpty() {
        return Optional.ofNullable(text()).orElse("");
    }
    /**
     * 获取单元格数值，空值和非数字默认为null
     * @return {@link Num}
     */
    public Num number() {
        return value().map(Num::of).orElse(null);
    }

    /**
     * 获取单元格数值，空值和非数字默认为0
     * @return {@link Num}
     */
    public Num numberOfZore() {
        return value().map(v -> Num.of(v.toString(), 0)).orElse(Num.of(0));
    }

    /**
     * 获取单元格日期对象
     * @return {@link Dates}
     */
    public Dates date() {
        return value().map(v -> Num.of(v.toString()).toDate()).orElse(null);
    }
    /**
     * 获取公式 不使用占位符替换行号
     * @return String
     */
    public String formula() {
        return formula(false);
    }

    /**
     * 获取公式
     * @param holder boolean 公式中间的行号是否使用{0}占位；true是，false否
     * @return String
     */
    public String formula(boolean holder) {
        if (cellEmpty()) return null;
        if(Objects.equals(CellType.FORMULA, cell.getCellTypeEnum()))
            return holder
                    ? cell.getCellFormula().replaceAll("(?<=[A-Z])" + (rowIndex + 1), "{0}") // 获取到的公式将会使用正则替换为行占位符
                    : cell.getCellFormula();
        return null;
    }

    /**
     * 获取样式索引
     * @return Integer
     */
    public Integer sindex() {
        return Objects.isNull(cell) ? null : (int) cell.getCellStyle().getIndex();
    }

    /**
     * 获取格式化之后的字符串
     * @return String
     */
    public String dataFormat() {
        return Objects.isNull(cell) ? null : cell.getCellStyle().getDataFormatString();
    }

    /**
     * 获取单元格数据类型
     * @return {@link DataType}
     */
    public DataType type() {
        if(Objects.isNull(cell)) return null;
        switch (cell.getCellTypeEnum()){
            case NUMERIC:
                if(DateUtil.isCellDateFormatted(cell)) return DataType.Date;
                else if(Optional.ofNullable(dataFormat()).orElse("").endsWith("%")) return DataType.Percent;
                else return DataType.Number;
            case FORMULA:
                if(CellType.NUMERIC == cell.getCachedFormulaResultTypeEnum()) return DataType.Number;
        }
        return DataType.Text;
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
//                System.out.println(FWrite.build().to(Path.TEMP.file(file.getName() + ".html")).write(sb.toString()).getFilePath().orElse(null));
            };
//            read.accept(Path.EXCEL.file("test.xlsx"));
//            read.accept(Path.EXCEL.file("test.xls"));
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
//                System.out.println(FWrite.build().to(Path.TEMP.file(file.getName() + ".json")).writeJson(map).getFilePath().orElse(null));
            };
//            read.accept(Path.EXCEL.file("地区划分.xlsx"));
//            read.accept(Path.EXCEL.file("地区划分.xls"));
        }
    }
}
