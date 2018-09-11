package com.utils.util.excel;

import com.utils.common.entity.excel.Position;
import lombok.NonNull;

import java.util.Objects;

/**
 * Sheet 读操作相关的方法封装
 *
 * @author Jason Xie on 2018-8-8.
 */
public interface ISheetReader<T extends ISheetReader> extends ISheet<T> {

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
     * 指定当前操作单元格
     *
     * @param position Position 单元格坐标
     * @return <T extends ISheetReader>
     */
    default T cell(@NonNull final Position position) {
        row(position.rowIndex()).cell(position.columnIndex());
        return (T) this;
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
        if(hasEnd()) return null;
        row(getRowIndex());
        if (Objects.isNull(getRow()))
            return next();
        return (T) this;
    }


}