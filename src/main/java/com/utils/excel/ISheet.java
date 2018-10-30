package com.utils.excel;

import com.utils.excel.entity.Position;
import com.utils.excel.enums.Column;
import com.utils.util.FPath;
import com.utils.util.Maps;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Sheet 读写需要的基本方法
 *
 * @author Jason Xie on 2018-8-8.
 */
public interface ISheet<T> {
    /**
     * 当前操作对象作为参数，执行完之后返回当前对象；
     * 没啥特别的作用，只是为了让一些不能使用链式一直写完的代码可以包在链式调用里面；
     *
     * @param consumer Consumer<T>
     * @return <T>
     */
    default T execute(final Consumer<T> consumer) {
        consumer.accept((T) this);
        return (T) this;
    }

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
     * 获取下一行行索引，getRowIndex() + 1
     *
     * @return int
     */
    default int getNextRowIndex() {
        return getRowIndex() + 1;
    }

    /**
     * 获取行号，getRownum() = getRowIndex() + 1
     *
     * @return int
     */
    default int getRownum() {
        return getRowIndex() + 1;
    }

    /**
     * 获取下一行行号，getRownum() = getRowIndex() + 2
     *
     * @return int
     */
    default int getNextRownum() {
        return getRowIndex() + 2;
    }

    /**
     * 设置当前操作行索引
     *
     * @param rowIndex int
     * @return <T extends ISheet>
     */
    T setRowIndex(final int rowIndex);

    /**
     * 以当前行索引为基础，跳过指定行数
     *
     * @param count int 跳过行数
     * @return <T extends ISheet>
     */
    default T skip(final int count) {
        setRowIndex(getRowIndex() + count - 1);
        return (T) this;
    }

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
     * 指定当前操作单元格
     *
     * @param position Position 单元格坐标
     * @return <T extends ISheet>
     */
    default T cell(final Position position) {
        Objects.requireNonNull(position, "参数【position】是必须的");
        row(position.rowIndex());
        cell(position.columnIndex());
        return (T) this;
    }

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
    default T row(final Rownum rownum) {
        Objects.requireNonNull(rownum, "参数【rownum】是必须的");
        row(rownum.index());
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
     * 选择操作单元格
     *
     * @param column {@link Column} 列名枚举定义
     * @return <T extends ISheet>
     */
    default T cell(final Column column) {
        cell(column.ordinal());
        return (T) this;
    }

    /**
     * 获取当前操作行所有列的数据类型，便于后面写入时确定数据类型
     *
     * @return {@link Map<Integer:列索引, CellType:单元格类型>}
     */
    default Map<Integer, CellType> cellTypes() {
        final Map<Integer, CellType> cellTypes = new HashMap<>(20);
        getRow().forEach(cell -> cellTypes.put(cell.getColumnIndex(), cell.getCellType()));
        return cellTypes;
    }

    /**
     * 获取当前 Sheet 所有comments
     *
     * @return {@link Map<String:A1单元格坐标, String:批注>
     */
    default Map<String, String> comments() {
        final Map<CellAddress, ? extends Comment> cellComments = getSheet().getCellComments();
        return cellComments.entrySet().stream()
                .map(entry ->
                        Maps.bySS(entry.getKey().formatAsString(), entry.getValue().getString().getString())
                )
                .reduce((s, v) -> {
                    s.putAll(v);
                    return s;
                })
                .orElseGet(HashMap::new);
    }

    /**
     * 保存到指定路径
     *
     * @param path {@link FPath} 保存路径
     * @return {@link FPath}
     */
    @SneakyThrows
    default FPath saveWorkBook(final FPath path) {
        @Cleanup final FileOutputStream fileOutputStream = new FileOutputStream(path.file());
        getWorkbook().write(fileOutputStream);
        path.chmod(644); // 设置文件权限
        return path;
    }

    /**
     * 关闭 Workbook 对象
     */
    @SneakyThrows
    default void close() {
        getWorkbook().close();
    }
}