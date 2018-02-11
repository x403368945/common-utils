package com.utils.common.entity.excel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.utils.enums.DataType;
import com.utils.util.Dates;
import com.utils.util.Num;
import com.utils.util.Util;

import java.util.List;
import java.util.Objects;

/**
 * excel 数据行（解析得到数据行，也可以自由构建数据行）
 *
 * @author Jason Xie on 2017/11/3.
 */
public class Row extends JSONObject {
    public static Row build() {
        return new Row();
    }

    /**
     * 新增单元格
     *
     * @param key   int
     * @param value Cell
     * @return Row
     */
    public Row addCell(int key, Cell value) {
        return addCell("" + key, value);
    }

    /**
     * 新增单元格
     *
     * @param key   String
     * @param value Cell
     * @return Row
     */
    public Row addCell(String key, Cell value) {
        if (Objects.nonNull(key) && Objects.nonNull(value)) super.put(key, value);
        return this;
    }

    /**
     * 添加子节点集合
     *
     * @param rows List<Row>
     * @return Row
     */
    public Row addChilds(final List<Row> rows) {
        if (Util.isNotEmpty(rows)) super.put("childs", rows);
        return this;
    }

    /**
     * 添加子节点表格
     *
     * @param table Table
     * @return Row
     */
    public Row addTable(final Table table) {
        if (Util.isNotEmpty(table)) super.put("table", table);
        return this;
    }
    /**
     * 复制单元格，将某行的单元格复制到当前行
     *
     * @param values Row
     * @return Row
     */
    public Row copy(Row values) {
        if (Objects.nonNull(values)) super.putAll(values);
        return this;
    }


    /**
     * 获取子节点集合
     *
     * @return List<Row>
     */
    @JSONField(serialize = false, deserialize = false)
    public List<Row> getChilds() {
        return super.containsKey("childs") ? JSON.parseArray(super.getString("childs"), Row.class) : null;
    }

    /**
     * 获取单元格对象
     *
     * @param key String
     * @return Cell
     */
    @JSONField(serialize = false, deserialize = false)
    public Cell getCell(final String key) {
        return super.containsKey(key) ? super.getObject(key, Cell.class) : null;
    }

    /**
     * 获取单元格对象
     *
     * @param key Object
     * @return Cell
     */
    @JSONField(serialize = false, deserialize = false)
    public Cell getCell(final Object key) {
        return getCell(Objects.toString(key, null));
    }

    /**
     * 获得单元格数据类型
     *
     * @param key Object
     * @return DataType
     */
    @JSONField(serialize = false, deserialize = false)
    public DataType getCellType(final Object key) {
        Cell cell = getCell(key);
        return Objects.isNull(cell) ? null : cell.getType();
    }

    /**
     * 获得单元格文本
     *
     * @param key Object
     * @return String
     */
    @JSONField(serialize = false, deserialize = false)
    public String getCellText(final Object key) {
        Cell cell = getCell(key);
        return Objects.isNull(cell) ? null : cell.getText();
    }

    /**
     * 获得单元格 value ，且将 value 转换为 Datas 日期处理对象
     *
     * @param key Object
     * @return Dates
     */
    @JSONField(serialize = false, deserialize = false)
    public Dates getCellDate(final Object key) {
        Cell cell = getCell(key);
        return Objects.isNull(cell) ? null : cell.getDate();
    }

    /**
     * 获得单元格 value ，且将 value 转换为 Num 数字处理对象
     *
     * @param key Object
     * @return Num
     */
    @JSONField(serialize = false, deserialize = false)
    public Num getCellNumber(final Object key) {
        Cell cell = getCell(key);
        return Objects.isNull(cell) ? null : cell.getNumber();
    }

    @Override
    public Row clone() {
        Row row = Row.build();
        row.putAll((JSONObject) super.clone());
        return row;
    }
}
