package com.utils.util.excel;

import com.utils.util.FPath;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.*;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Sheet 读写需要的基本方法
 *
 * @author Jason Xie on 2018-8-8.
 */
public interface ISheet<T> {
    /**
     * 获取当前操作Workbook
     *
     * @return {@link Workbook}
     */
    Workbook getWorkbook();

    /**
     * 获取当前操作Sheet
     *
     * @return {@link Sheet}
     */
    Sheet getSheet();

    /**
     * 获取当前操作行
     *
     * @return {@link Row}
     */
    Row getRow();

    /**
     * 获取行索引，getRowIndex() = getRow().getRowNum()
     *
     * @return int
     */
    int getRowIndex();

    /**
     * 获取行号，getRownum() = getRowIndex() + 1
     *
     * @return int
     */
    default int getRownum() {
        return getRowIndex() + 1;
    }

    /**
     * 设置当前操作行索引
     *
     * @param rowIndex int
     * @return <T extends ISheet>
     */
    T setRowIndex(final int rowIndex);

    /**
     * 设置当前操作行
     *
     * @param row {@link Row}
     * @return <T extends ISheet>
     */
    T row(final Row row);

    /**
     * 指定当前操作单元格
     *
     * @param cell {@link Cell}
     * @return <T extends ISheet>
     */
    T cell(final Cell cell);

    /**
     * 选择操作行
     *
     * @param rowIndex int 行索引
     * @return <T extends ISheet>
     */
    default T row(final int rowIndex) {
        setRowIndex(rowIndex);
        return row(getSheet().getRow(rowIndex));
    }

    /**
     * 选择操作行
     *
     * @param rownum Rownum 数据行
     * @return <T extends ISheet>
     */
    default T row(@NonNull final Rownum rownum) {
        row(rownum.rowIndex());
        return (T) this;
    }

    /**
     * 选择操作单元格
     *
     * @param columnIndex int 列索引
     * @return <T extends ISheet>
     */
    default T cell(final int columnIndex) {
        cell(Objects.isNull(getRow()) ? null : getRow().getCell(columnIndex));
        return (T) this;
    }

    /**
     * 获取当前操作行所有列的数据类型，便于后面写入时确定数据类型
     *
     * @return Map<Integer                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               [                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               columnIndex                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               ]                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               ,                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               CellType>
     */
    default Map<Integer, CellType> cellTypes() {
        final Map<Integer, CellType> cellTypes = new HashMap<>();
        getRow().forEach(cell -> cellTypes.put(cell.getColumnIndex(), cell.getCellTypeEnum()));
        return cellTypes;
    }

    @SneakyThrows
    default FPath saveWorkBook(final FPath path) {
        @Cleanup final FileOutputStream fileOutputStream = new FileOutputStream(path.file());
        getWorkbook().write(fileOutputStream);
        path.chmod(644); // 设置文件权限
//        System.out.println(String.format("写入路径：%s", path.absolute()));
        return path;
    }
}