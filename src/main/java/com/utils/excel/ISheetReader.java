package com.utils.excel;

import com.utils.excel.entity.Cell;
import com.utils.excel.enums.DataType;

import java.util.*;
import java.util.function.Consumer;

/**
 * Sheet 读操作相关的方法封装
 *
 * @author 谢长春 on 2018-8-8 .
 */
@SuppressWarnings("unchecked")
public interface ISheetReader<T extends ISheetReader<T>> extends ISheet<T>, ICellReader<T> {

    /**
     * 数据是否已读完
     *
     * @return boolean true：最后一行已经读完
     */
    default boolean hasEnd() {
        return getRowIndex() > getSheet().getLastRowNum();
    }

    /**
     * 获取最后一行索引
     *
     * @return int
     */
    default int getLastRowIndex() {
        return getSheet().getLastRowNum();
    }

    /**
     * 换行操作<br>
     * 警告：当调用 next() 方法之后，先执行 setRowIndex() ，判断 hasEnd() 之后会跳出，但当前操作的 Row 对象还在上一行；
     * 下次操作时需要重新指定操作行，否则将会操作 hasEnd() 之前的那一行数据
     *
     * @return <T extends ISheetReader>
     */
    default T next() {
        setRowIndex(getRowIndex() + 1); // 设置下一行 rowIndex ，判断是否已读完
        if (hasEnd()) {
            return null;
        }
        row(getRowIndex());
        if (Objects.isNull(getRow())) {
            return next();
        }
        return (T) this;
    }

    /**
     * 判断是否已经读完
     *
     * @return boolean true：已经读完了
     */
    default boolean hasNext() {
        return Objects.nonNull(next());
    }

    /**
     * 判断是否已经读完
     *
     * @param ending {@link Consumer}{@link Consumer<Integer:rowIndex>} 最后一行读完之后触发该操作，参数为最后一行索引
     * @return boolean false：已经读完了
     */
    default boolean hasNext(final Consumer<Integer> ending) {
        if (Objects.isNull(next())) {
            if (Objects.nonNull(ending)) {
                ending.accept(getRowIndex() - 1);
            }
            return false;
        }
        return true;
    }

    /**
     * 获取头部列名加索引
     *
     * @return {@link Cell}
     */
    default List<Cell> headers() {
        final List<Cell> headers = new ArrayList<>();
        String label;
        for (int i = 0; i < getRow().getLastCellNum(); i++) {
            cell(i);
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(label = stringValue())) {
                headers.add(Cell.builder().index(i).label(label.trim()).type(DataType.TEXT).sindex(sindex()).build());
            }
        }
        return headers;
    }

    /**
     * 获取头部列名加索引
     * 警告：重复的列名将会被覆盖；若不能保证列名不重复，请使用 {@link ISheetReader#headers()}
     *
     * @return {@link Map}{@link Map<String:单元格文本内容, Integer:单元格列索引>}
     */
    default LinkedHashMap<String, Integer> mapHeaders() {
        final LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < getRow().getLastCellNum(); i++) {
            map.put(cell(i).stringOfEmpty().trim(), i);
        }
        map.remove("");
        return map;
    }

}
