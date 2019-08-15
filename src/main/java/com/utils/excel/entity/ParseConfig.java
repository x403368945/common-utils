package com.utils.excel.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.utils.IJson;
import com.utils.excel.enums.DataType;
import com.utils.util.Maps;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据表格头部<br>
 * {index: 0, label: '房租', type: 'Number', group: "固定成本",tag:"标签"}<br>
 *
 * @author 谢长春 on 2017/10/15 .
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Accessors(chain = true)
@JSONType(orders = {"index", "label", "type", "group", "tag"})
@Slf4j
public class ParseConfig implements IJson {
    /**
     * 数据起始行行号，非索引
     */
    private int startRowNum;
    /**
     * <pre>
     * 数据表头，数据结构示例：
     * [
     * {index: 0, label: '房租', type: 'Number', group: "固定成本",tag:"标签"},
     * {index: 1, label: '工资', type: 'Number', group: "固定成本",tag:"标签"}
     * ]
     */
    private List<Cell> header;
    /**
     * <pre>
     * 数据表合计行配置
     * 不是必须配置项，没有footer就不用配置
     * {@link Map}{@link Map<String:key, String:value>}
     */
    private Map<String, String> footer;
    /**
     * <pre>
     * 扩展字段坐标
     * 不是必须配置项，没有扩展属性就不用配置
     * 数据结构示例：
     * {"dept":"B2","seq":"G2","year":"O2","compileDept":"T2","compileUser":"Z2","compileTime":"AF2"}
     */
    private Object extras;
//
//    /**
//     * 科目分组<br>
//     * 数据结构示例：<br>
//     [
//     {
//     "code": "1001", "name": "业务招待费",
//     "childs": [
//     {"code": "100101", "name": "餐饮", "parentCode": "1001", "parentName": "业务招待费"},
//     {"code": "100102", "name": "礼品", "parentCode": "1001", "parentName": "业务招待费"},
//     {"code": "100103", "name": "其他", "parentCode": "1001", "parentName": "业务招待费"}
//     ]
//     },
//     {
//     "code": "1002", "name": "差旅费",
//     "childs": [
//     {"code": "100201", "name": "住宿费", "parentCode": "1002", "parentName": "差旅费"},
//     {"code": "100202", "name": "交通费", "parentCode": "1002", "parentName": "差旅费"},
//     {"code": "100203", "name": "补助费", "parentCode": "1002", "parentName": "差旅费"},
//     {"code": "100204", "name": "其他", "parentCode": "1002", "parentName": "差旅费"}
//     ]
//     }
//     ]
//     */
//    private List<JSONObject> groups;

    /**
     * <pre>
     * 设置合计行字段名和列坐标
     * param label String 合计单元格文本，默认：合计
     * param column 合计单元格文本所在列，默认：A 列
     */
    public ParseConfig setFooter() {
        this.footer = defaultFooter();
        return this;
    }

    /**
     * 设置合计行字段名和列坐标
     *
     * @param label  {@link String} 合计单元格文本【合计】
     * @param column {@link String} 合计单元格文本所在列【B】
     */
    public ParseConfig setFooter(final String label, final String column) {
        this.footer = Maps.ofSS().put("label", label).put("column", column).build();
        return this;
    }

    /**
     * 设置合计行字段名和列坐标
     *
     * @param column {@link String} 合计单元格文本所在列【B】
     */
    public ParseConfig setFooter(final String column) {
        this.footer = Maps.ofSS().put("label", "合计").put("column", column).build();
        return this;
    }

    /**
     * 获取底部合计文字单元格坐标
     *
     * @return {@link Position}
     */
    @JSONField(serialize = false, deserialize = false)
    public Position getFooterColumnAddress() {
        return Position.builder().column(getFooter().get("column")).build();
    }

    public static Map<String, String> footer(final String column) {
        return Maps.ofSS().put("label", "合计").put("column", column).build();
    }

    public static Map<String, String> footer(final String label, final String column) {
        return Maps.ofSS().put("label", label).put("column", column).build();
    }

    public static Map<String, String> defaultFooter() {
        return footer("合计", "A");
    }

    @Override
    public String toString() {
        return json();
    }

    public static void main(String[] args) {
        ParseConfig config = new ParseConfig();
        config.setStartRowNum(6);
        int index = 1;
        {
            List<Cell> headers = new ArrayList<>();
            Cell.CellBuilder builder = Cell.builder();
            builder.group("主营业务收入");
            headers.add(builder.index(index++).label("产品/项目/服务名称").type(DataType.TEXT).build());
            headers.add(builder.index(index++).label("规格型号").type(DataType.TEXT).build());
            headers.add(builder.index(index++).label("单价").type(DataType.NUMBER).build());
            headers.add(builder.index(index++).label("上年实际").type(DataType.NUMBER).build());
            headers.add(builder.index(index++).label("本年预算").type(DataType.NUMBER).build());

            builder.group("第一季度").type(DataType.NUMBER);
            headers.add(builder.index(index++).label("1月").tag("销售量").build());
            headers.add(builder.index(index++).label("1月").tag("销售额").build());
            headers.add(builder.index(index++).label("2月").tag("销售量").build());
            headers.add(builder.index(index++).label("2月").tag("销售额").build());
            headers.add(builder.index(index++).label("3月").tag("销售量").build());
            headers.add(builder.index(index++).label("3月").tag("销售额").build());
            headers.add(builder.index(index++).label("合计").tag("销售量").build());
            headers.add(builder.index(index++).label("合计").tag("销售额").build());

            builder.group("第二季度").type(DataType.NUMBER);
            headers.add(builder.index(index++).label("4月").tag("销售量").build());
            headers.add(builder.index(index++).label("4月").tag("销售额").build());
            headers.add(builder.index(index++).label("5月").tag("销售量").build());
            headers.add(builder.index(index++).label("5月").tag("销售额").build());
            headers.add(builder.index(index++).label("6月").tag("销售量").build());
            headers.add(builder.index(index++).label("6月").tag("销售额").build());
            headers.add(builder.index(index++).label("合计").tag("销售量").build());
            headers.add(builder.index(index++).label("合计").tag("销售额").build());

            builder.group("第三季度").type(DataType.NUMBER);
            headers.add(builder.index(index++).label("7月").tag("销售量").build());
            headers.add(builder.index(index++).label("7月").tag("销售额").build());
            headers.add(builder.index(index++).label("8月").tag("销售量").build());
            headers.add(builder.index(index++).label("8月").tag("销售额").build());
            headers.add(builder.index(index++).label("9月").tag("销售量").build());
            headers.add(builder.index(index++).label("9月").tag("销售额").build());
            headers.add(builder.index(index++).label("合计").tag("销售量").build());
            headers.add(builder.index(index++).label("合计").tag("销售额").build());

            builder.group("第四季度").type(DataType.NUMBER);
            headers.add(builder.index(index++).label("10月").tag("销售量").build());
            headers.add(builder.index(index++).label("10月").tag("销售额").build());
            headers.add(builder.index(index++).label("11月").tag("销售量").build());
            headers.add(builder.index(index++).label("11月").tag("销售额").build());
            headers.add(builder.index(index++).label("12月").tag("销售量").build());
            headers.add(builder.index(index++).label("12月").tag("销售额").build());
            headers.add(builder.index(index++).label("合计").tag("销售量").build());
            headers.add(builder.index(index++).label("合计").tag("销售额").build());
            config.setHeader(headers);
        }
        config.setFooter();
        config.setExtras(JSON.parseObject("{\"dept\":\"B2\",\"seq\":\"G2\",\"year\":\"O2\",\"compileDept\":\"T2\",\"compileUser\":\"Z2\",\"compileTime\":\"AF2\"}"));
        log.info("{}", config);
        log.info("{}", config.getFooterColumnAddress().columnIndex());
    }
}
