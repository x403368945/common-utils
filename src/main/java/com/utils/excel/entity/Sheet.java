package com.utils.excel.entity;

import com.alibaba.fastjson.annotation.JSONType;
import com.utils.IJson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Excel sheet 实体对象
 * @author 谢长春 on 2017/10/15 .
 */
@NoArgsConstructor
@Data
@Accessors(chain = true)
@JSONType(orders = {"index","name","table"})
public class Sheet implements IJson {
    /**
     * excel sheet 标签页索引
     */
    private int index;
    /**
     * excel sheet 标签页名称
     */
    private String name;
    /**
     * excel sheet 标签页内容
     */
    private Table table;

    @Override
    public String toString() {
        return json();
    }
}
