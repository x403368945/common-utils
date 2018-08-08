package com.utils.util.excel;

import com.utils.common.entity.excel.Position;
import lombok.*;

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
     * 换行
     *
     * @return <T extends ISheetReader>
     */
    default T next() {
        if (!hasEnd()) {
            row(getRowIndex() + 1);
            if (Objects.isNull(getRow())) next();
        }
        return (T) this;
    }


}