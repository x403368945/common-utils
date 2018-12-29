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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Sheet 读写需要的基本方法
 *
 * @author 谢长春 on 2018-8-8.
 */
public interface ISheet<T> {
    /**
     * 当前操作对象作为参数，执行完之后返回当前对象；
     * 没啥特别的作用，只是为了让一些不能使用链式一直写完的代码可以包在链式调用里面；
     *
     * @param consumer {@link Consumer}{@link Consumer<T>}
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
     * 判断sheet是否存在
     *
     * @return {@link Optional}{@link Optional<ISheet>}
     */
    default Optional<T> hasSheet() {
        if (Objects.isNull(getSheet())) {
            return Optional.empty();
        }
        return Optional.of((T) this);
    }

    /**
     * 判断sheet是否存在，不存在则抛出用户指定异常，若不指定异常，则不抛出
     *
     * @param ex {@link Supplier}{@link Supplier<RuntimeException>} 自定义异常
     * @return <T extends ISheet>
     */
    default T hasSheet(final Supplier<? extends RuntimeException> ex) {
        if (Objects.isNull(getSheet())) {
            if (Objects.nonNull(ex)) throw ex.get();
        }
        return (T) this;
    }

    /**
     * 以当前行索引为基础，跳过指定行数
     *
     * @param hasTrue  {@link Consumer}{@link Consumer<ISheet>} 为 true 时执行
     * @param hasFalse {@link Consumer}{@link Consumer<ISheet>} 为 false 时执行
     * @return <T extends ISheet>
     */
    default T hasSheet(final Consumer<T> hasTrue, final Consumer<T> hasFalse) {
        if (Objects.nonNull(getSheet())) {
            if (Objects.nonNull(hasTrue)) hasTrue.accept((T) this);
        } else {
            if (Objects.nonNull(hasFalse)) hasFalse.accept((T) this);
        }
        return (T) this;
    }

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
     * @return {@link Map}{@link Map<Integer:列索引, CellType:单元格类型>}
     */
    default Map<Integer, CellType> cellTypes() {
        final Map<Integer, CellType> cellTypes = new HashMap<>(20);
        getRow().forEach(cell -> cellTypes.put(cell.getColumnIndex(), cell.getCellType()));
        return cellTypes;
    }

    /**
     * 获取当前 Sheet 所有comments
     *
     * @return {@link Map>{@link Map<String:A1单元格坐标, String:批注>
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
     * 判断 sheet 是否为隐藏状态
     *
     * @return boolean true：隐藏
     */
    default boolean isHiddenSheet() {
        return getWorkbook().isSheetHidden(getWorkbook().getSheetIndex(getSheet()));
    }

    /**
     * 获取 sheet 总数
     *
     * @return int
     */
    default int sheetCount() {
        return getWorkbook().getNumberOfSheets();
    }

    /**
     * 获取 sheet 索引
     *
     * @return int
     */
    default int sheetIndex() {
        return getWorkbook().getSheetIndex(getSheet());
    }

    /**
     * 获取 sheet 名称
     *
     * @return {@link String}
     */
    default String sheetName() {
        return getSheet().getSheetName();
    }

    /**
     * 选中当前 sheet
     *
     * @return <T extends ISheet>
     */
    default T selectedSheet() {
        getWorkbook().setSelectedTab(sheetIndex());
        return (T) this;
    }

    /**
     * 选中指定 sheet
     *
     * @param index int 指定 sheet 索引
     * @return <T extends ISheet>
     */
    default T selectedSheet(final int index) {
        getWorkbook().setSelectedTab(index);
        return (T) this;
    }


    /**
     * 选中指定 sheet
     *
     * @param name String 指定 sheet 名字
     * @return <T extends ISheet>
     */
    default T selectedSheet(final String name) {
        getWorkbook().setSelectedTab(getWorkbook().getSheetIndex(name));
        return (T) this;
    }

    /**
     * 关闭 Workbook 对象
     */
    @SneakyThrows
    default void close() {
        getWorkbook().close();
    }

}