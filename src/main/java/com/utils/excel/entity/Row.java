package com.utils.excel.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.utils.excel.enums.DataType;
import com.utils.util.Dates;
import com.utils.util.Maps;
import com.utils.util.Num;
import com.utils.util.Util;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * excel 数据行（解析得到数据行，也可以自由构建数据行）
 *
 * @author 谢长春 on 2017/11/3 .
 */
@NoArgsConstructor
public class Row extends JSONObject {
    /**
     * 指定是否使用 {@link LinkedHashMap}
     *
     * @param ordered {@link Boolean} true:{@link LinkedHashMap}, false:{@link HashMap}
     */
    public Row(final boolean ordered){
        super(ordered);
    }

    public static Row build() {
        return new Row(true);
    }

    /**
     * 新增单元格
     *
     * @param key   int
     * @param value {@link Cell} 单元格对象
     * @return {@link Row}
     */
    public Row addCell(int key, Cell value) {
        return addCell("" + key, value);
    }

    /**
     * 新增单元格
     *
     * @param key   String
     * @param value {@link Cell} 单元格对象
     * @return {@link Row}
     */
    public Row addCell(String key, Cell value) {
        if (Objects.nonNull(key) && Objects.nonNull(value)) {
            super.put(key, value);
        }
        return this;
    }

    /**
     * 添加子节点集合
     *
     * @param rows {@link List}{@link List<Row:数据行对象>}
     * @return {@link Row}
     */
    public Row addChilds(final List<Row> rows) {
        if (Util.isNotEmpty(rows)) {
            super.put("childs", rows);
        }
        return this;
    }

    /**
     * 添加子节点表格
     *
     * @param table {@link Table} 数据表格对象
     * @return {@link Row}
     */
    public Row addTable(final Table table) {
        if (Util.isNotEmpty(table)) {
            super.put("table", table);
        }
        return this;
    }

    /**
     * 复制单元格，将某行的单元格复制到当前行
     *
     * @param values {@link Row} 数据行对象
     * @return {@link Row}
     */
    public Row copy(Row values) {
        if (Objects.nonNull(values)) {
            super.putAll(values);
        }
        return this;
    }


    /**
     * 获取子节点集合
     *
     * @return {@link Row}
     */
    @JSONField(serialize = false, deserialize = false)
    public List<Row> getChilds() {
        return super.containsKey("childs") ? JSON.parseArray(super.getString("childs"), Row.class) : null;
    }

    /**
     * 获取单元格对象
     *
     * @param key String
     * @return {@link Cell}
     */
    @JSONField(serialize = false, deserialize = false)
    public Cell getCell(final String key) {
        return super.containsKey(key) ? super.getObject(key, Cell.class) : null;
    }

    /**
     * 获取单元格对象
     *
     * @param key Object
     * @return {@link Cell}
     */
    @JSONField(serialize = false, deserialize = false)
    public Cell getCell(final Object key) {
        return getCell(Objects.toString(key, null));
    }

    /**
     * 获得单元格数据类型
     *
     * @param key Object
     * @return {@link DataType}
     */
    @JSONField(serialize = false, deserialize = false)
    public DataType getCellType(final Object key) {
        final Cell cell = getCell(key);
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
        final Cell cell = getCell(key);
        return Objects.isNull(cell) ? null : cell.getText();
    }

    /**
     * 获得单元格 value ，且将 value 转换为 Datas 日期处理对象
     *
     * @param key Object
     * @return {@link Dates}
     */
    @JSONField(serialize = false, deserialize = false)
    public Dates getCellDate(final Object key) {
        final Cell cell = getCell(key);
        return Objects.isNull(cell) ? null : cell.date();
    }

    /**
     * 获得单元格 value ，且将 value 转换为 Num 数字处理对象
     *
     * @param key Object
     * @return {@link Num}
     */
    @JSONField(serialize = false, deserialize = false)
    public Num getCellNumber(final Object key) {
        final Cell cell = getCell(key);
        return Objects.isNull(cell) ? null : cell.number();
    }

    /**
     * <pre>
     * 使用 alias 或 label 作为key， value 或 text 作为 value ，转换为 {@link Map}{@link Map<String, String>}
     * key:String:alias|label,
     * value:String:value|text
     *
     * @param header {@link List<Cell>} 表头定义
     * @return {@link Map<String, String>}
     */
    public Map<String, String> toMapString(final List<Cell> header) {
        return header.stream()
                .map(head -> Optional.ofNullable(getCell(head.index()))
                        .map(cell -> Objects.toString(cell.getValue(), cell.getText()))
                        .map(v -> Maps.bySS(Optional.ofNullable(head.getAlias()).orElseGet(head::getLabel), v))
                        .orElse(Collections.emptyMap())
                )
                .reduce(new LinkedHashMap<>(), (s, v) -> {
                    s.putAll(v);
                    return s;
                });
    }

    /**
     * <pre>
     * 使用 alias 或 label 作为key， value 或 text 作为 value ，转换为 {@link Map}{@link Map<String, Object>}
     * key:String:alias|label,
     * value:Object:value|text
     *
     * @param header {@link List<Cell>} 表头定义
     * @return {@link Map<String, Object>}
     */
    public Map<String, Object> toMapObject(final List<Cell> header) {
        return header.stream()
                .map(head -> Optional.ofNullable(getCell(head.index()))
                        .map(cell -> Optional.ofNullable(cell.getValue()).orElseGet(cell::getText))
                        .map(v -> Maps.bySO(Optional.ofNullable(head.getAlias()).orElseGet(head::getLabel), v))
                        .orElse(Collections.emptyMap())
                )
                .reduce(new LinkedHashMap<>(), (s, v) -> {
                    s.putAll(v);
                    return s;
                });
    }

    /**
     * <pre>
     * 使用 alias 或 label 作为key， value 或 text 作为 value ，转换为 {@link Map}{@link Map<String, Object>}
     * key:String:alias|label,
     * value:Object:value|text
     *
     * @param header {@link List<Cell>} 表头定义
     * @return {@link Map<String, Object>}
     */
    public JSONObject toJSONObject(final List<Cell> header) {
        return header.stream()
                .map(head -> Optional.ofNullable(getCell(head.index()))
                        .map(cell -> Optional.ofNullable(cell.getValue()).orElseGet(cell::getText))
                        .map(v -> Maps.ofSO().put(Optional.ofNullable(head.getAlias()).orElseGet(head::getLabel), v).buildJSONObject())
                        .orElse(new JSONObject(true))
                )
                .reduce(new JSONObject(true), (s, v) -> {
                    s.putAll(v);
                    return s;
                });
    }


    @Override
    public Row clone() {
        final Row row = Row.build();
        row.putAll((JSONObject) super.clone());
        return row;
    }
}
