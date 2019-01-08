package com.utils.excel.entity;

import com.alibaba.fastjson.annotation.JSONType;
import com.utils.IJson;
import com.utils.excel.enums.DataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据表格头部<br>
 * {index: 0, label: '房租', type: 'Number', group: "固定成本",tag:"标签"}<br>
 * @author 谢长春 on 2017/10/15 .
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Accessors(chain = true)
@JSONType(orders = {"index", "label", "type", "sindex", "group", "tag", "hidden", "required"})
@Slf4j
public class Header implements IJson {
    /**
     * 数据列索引
     */
    private int index;
    /**
     * 数据列名
     */
    private String label;
    /**
     * 数据类型
     */
    private DataType type;
    /**
     * Excel 单元格样式索引
     */
    private Integer sindex;
    /**
     * 数据列所属分组
     */
    private String group;
    /**
     * 数据列标签
     */
    private String tag;
    /**
     * 当前列是否隐藏
     */
    private Boolean hidden;
    /**
     * 是否必填
     */
    private Boolean required;

    @Override
    public String toString() {
        return json();
    }

    public static void main(String[] args) {
        {
            HeaderBuilder builder = Header.builder().label("列名").type(DataType.NUMBER).group("分组").tag("标签");
            Header header0 = builder.index(0).build();
            Header header1 = builder.index(1).build();
            Header header2 = builder.index(2).build();
            log.info("{}", header0);
            log.info("{}", header1);
            log.info("{}", header2);
        }
        {
            List<Header> headers = new ArrayList<>();
            Header.HeaderBuilder builder = Header.builder();
            int index = 0; // 索引从0开始
            {
                builder.group("主营业务收入");
                headers.add(builder.index(index++).label("产品/项目/服务名称").type(DataType.TEXT).build());
                headers.add(builder.index(index++).label("规格型号").type(DataType.TEXT).build());
                headers.add(builder.index(index++).label("单价").type(DataType.NUMBER).build());
                headers.add(builder.index(index++).label("上年实际").type(DataType.NUMBER).build());
                headers.add(builder.index(index++).label("本年预算").type(DataType.NUMBER).build());
            }
            {
                builder.group("第一季度").type(DataType.NUMBER);
                headers.add(builder.index(index++).label("1月").tag("销售量").build());
                headers.add(builder.index(index++).label("1月").tag("销售额").build());
                headers.add(builder.index(index++).label("2月").tag("销售量").build());
                headers.add(builder.index(index++).label("2月").tag("销售额").build());
                headers.add(builder.index(index++).label("3月").tag("销售量").build());
                headers.add(builder.index(index++).label("3月").tag("销售额").build());
                headers.add(builder.index(index++).label("合计").tag("销售量").build());
                headers.add(builder.index(index++).label("合计").tag("销售额").build());
            }
            {
                builder.group("第二季度").type(DataType.NUMBER);
                headers.add(builder.index(index++).label("4月").tag("销售量").build());
                headers.add(builder.index(index++).label("4月").tag("销售额").build());
                headers.add(builder.index(index++).label("5月").tag("销售量").build());
                headers.add(builder.index(index++).label("5月").tag("销售额").build());
                headers.add(builder.index(index++).label("6月").tag("销售量").build());
                headers.add(builder.index(index++).label("6月").tag("销售额").build());
                headers.add(builder.index(index++).label("合计").tag("销售量").build());
                headers.add(builder.index(index++).label("合计").tag("销售额").build());
            }
            {
                builder.group("第三季度").type(DataType.NUMBER);
                headers.add(builder.index(index++).label("7月").tag("销售量").build());
                headers.add(builder.index(index++).label("7月").tag("销售额").build());
                headers.add(builder.index(index++).label("8月").tag("销售量").build());
                headers.add(builder.index(index++).label("8月").tag("销售额").build());
                headers.add(builder.index(index++).label("9月").tag("销售量").build());
                headers.add(builder.index(index++).label("9月").tag("销售额").build());
                headers.add(builder.index(index++).label("合计").tag("销售量").build());
                headers.add(builder.index(index++).label("合计").tag("销售额").build());
            }
            {
                builder.group("第四季度").type(DataType.NUMBER);
                headers.add(builder.index(index++).label("10月").tag("销售量").build());
                headers.add(builder.index(index++).label("10月").tag("销售额").build());
                headers.add(builder.index(index++).label("11月").tag("销售量").build());
                headers.add(builder.index(index++).label("11月").tag("销售额").build());
                headers.add(builder.index(index++).label("12月").tag("销售量").build());
                headers.add(builder.index(index++).label("12月").tag("销售额").build());
                headers.add(builder.index(index++).label("合计").tag("销售量").build());
                headers.add(builder.index(index++).label("合计").tag("销售额").build());
            }
        }
    }
}
