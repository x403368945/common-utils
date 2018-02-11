package com.utils.common.entity.excel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONType;
import com.utils.common.entity.IJson;
import com.utils.common.entity.base.ResultInfo;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Excel sheet 表格实体对象
 * @author Jason Xie on 2017/10/15.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Accessors(chain = true)
@JSONType(orders = {"header","body","footer","extras"})
public class Table implements IJson {
    public static ResultInfo<Table> valueOf(@NonNull final String jsonText) {
        return JSON.parseObject(jsonText, new TypeReference<ResultInfo<Table>>() {});
    }
    /**
     * 表头集合 => <br>
     [
     {index: 0, label: '房租', type: 'NUMBER', group: "固定成本",tag:"标签"},
     {index: 1, label: '工资', type: 'NUMBER', group: "固定成本",tag:"标签"}
     ]
     */
    private List<Header> header;
    /**
     * 表格行集合, （0.1.....）表示表头中的index，取值时，可以通过遍历表头的index字段获取值<br>
     [
     {
     0: {type: 'NUMBER', text: "$1000", value: 1000},
     1: {type: 'NUMBER', text: "$1000", value: 1000}
     }
     ]
     */
    private List<Row> body;
    /**
     * 表格底部合计行,（0.1.....）表示表头中的index，取值时，可以通过遍历表头的index字段获取值<br>
     {
     0: {type: 'NUMBER', text: "$2000", value: 2000},
     1: {type: 'NUMBER', text: "$2000", value: 2000}
     }
     */
    private Row footer;
    /**
     * 表格附加字段（扩展属性）
     */
    private JSONObject extras;

    @Override
    public String toString() {
        return json();
    }
}
