package com.utils.excel.entity;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.utils.IJson;
import com.utils.excel.ICellWriter;
import com.utils.excel.ISheet;
import com.utils.excel.enums.Column;
import com.utils.excel.enums.DataType;
import com.utils.util.Dates;
import com.utils.util.Num;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

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
@JSONType(orders = {"address", "column", "index", "label", "alias", "type", "sindex", "group", "tag", "hidden", "required", "format", "comment", "text", "value", "formula"})
@Slf4j
public class Cell implements IJson {
    /**
     * 单元格绝对坐标：A1
     */
    private String address;
    /**
     * 数据列索引
     */
    private Column column;
    /**
     * 数据列索引
     */
    private Integer index;
    /**
     * 数据列名
     */
    private String label;
    /**
     * 数据列别名，可将实体类或数据库字段设置为别名
     */
    private String alias;
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
    /**
     * 列在页面展示时需要格式化：金额、数字、日期、使用 {} 占位的字符串替换
     */
    private String format;
    /**
     * 列说明
     */
    private String comment;

    /**
     * 单元格文本，格式化后的文本
     */
    private String text;
    /**
     * 单元格值，type为NUMBER、PERCENT时此属性为Double；type为DATE时，此属性为long(时间戳)；type为TEXT时，此属性为Null
     */
    private Object value;
    /**
     * 单元格公式
     */
    private String formula;

    /**
     * 获取列索引
     *
     * @return {@link Integer}
     */
    public Integer index() {
        if (Objects.nonNull(index)) return index;
        if (Objects.nonNull(column)) return column.ordinal();
        return null;
    }

    /**
     * 获取列枚举定义
     *
     * @return {@link Column}
     */
    public Column column() {
        if (Objects.nonNull(column)) return column;
        if (Objects.nonNull(index)) return Optional.of(Column.values())
                .map(arr -> index < arr.length ? arr[index] : null)
                .orElse(null);
        return null;
    }

    /**
     * 获取单元格绝对坐标
     *
     * @return {@link Position}
     */
    public Position position() {
        return Objects.isNull(address) ? null : Position.of(address);
    }


    /**
     * 将 value 转换为数字处理对象
     * 警告：value 可能为null，这里将返回绝对的数字处理对象，当 value 为 null 时，数字处理对象中的 value 也为null
     *
     * @return {@link Num}
     */
    @JSONField(serialize = false, deserialize = false)
    public Num number() {
        return Num.of(value);
    }

    /**
     * 将 value 转换为日期处理对象
     *
     * @return {@link Dates}
     */
    @JSONField(serialize = false, deserialize = false)
    public Dates date() {
        return Num.of(value).toDate();
    }

    /**
     * 单元格写入，优先使用 alias 从 row 对象中获取值，取不到再用 label 取值
     *
     * @param sheet {@link ISheet} 单元格写入器
     */
    public Cell cell(final ISheet sheet) {
        if (Objects.nonNull(address)) // 坐标不为空时优先使用
            sheet.cell(position());
        else
            sheet.cell(column);
        return this;
    }

    /**
     * 单元格写入，优先使用 alias 从 row 对象中获取值，取不到再用 label 取值
     *
     * @param writer {@link ICellWriter} T extends {@link ICellWriter} 单元格写入器
     * @param row    {@link JSONObject} 数据行对象
     */
    public <T extends ICellWriter> T write(final T writer, final JSONObject row) {
        writer.write(type, row.getOrDefault(alias, row.get(label)));
        return writer;
    }

    /**
     * 单元格写入，优先使用 alias 从 row 对象中获取值，取不到再用 label 取值
     *
     * @param writer {@link ICellWriter} T extends {@link ICellWriter} 单元格写入器
     * @param row    {@link JSONObject} 数据行对象
     * @param seq    {@link Supplier<Integer>} 带有序列
     */
    public <T extends ICellWriter> T write(final T writer, final JSONObject row, final Supplier<Integer> seq) {
        if (Objects.equals(type, DataType.SEQ)) {
            writer.writeNumber(seq.get());
        } else {
            write(writer, row);
        }
        return writer;
    }

    /**
     * 单元格写入，优先使用 alias 从 row 对象中获取值，取不到再用 label 取值；
     *
     * @param writer          {@link ICellWriter} T extends {@link ICellWriter} 单元格写入器
     * @param formulaFunction {@link Function<String:重构前的公式, String:重构后的公式>} 公式重构，替换占位符，默认将 formula 中 {column} 占位符替换为 {@link Column#name()}
     */
    public <T extends ICellWriter> T write(final T writer, final Function<String, String> formulaFunction) {
        writer.writeFormula(formulaFunction.apply(
                Optional.ofNullable(formula)
                        .orElse("")
                        .replaceAll("\\{column}", column.name())
        ));
        return writer;
    }

    /**
     * 单元格写入，优先使用 alias 从 row 对象中获取值，取不到再用 label 取值；
     *
     * @param writer          {@link ICellWriter} T extends {@link ICellWriter} 单元格写入器
     * @param row             {@link JSONObject} 数据行对象
     * @param formulaFunction {@link Function<String:重构前的公式, String:重构后的公式>} 公式重构，替换占位符，默认将 formula 中 {column} 占位符替换为 {@link Column#name()}
     */
    public <T extends ICellWriter> T write(final T writer, final JSONObject row, final Function<String, String> formulaFunction) {
        if (Objects.equals(type, DataType.FORMULA) && Objects.nonNull(formulaFunction)) {
            writer.writeFormula(formulaFunction.apply(
                    Optional.ofNullable(formula)
                            .orElse("")
                            .replaceAll("\\{column}", column.name())
            ));
        } else {
            write(writer, row);
        }
        return writer;
    }

    /**
     * 单元格写入，优先使用 alias 从 row 对象中获取值，取不到再用 label 取值；
     *
     * @param writer          {@link ICellWriter} T extends {@link ICellWriter} 单元格写入器
     * @param row             {@link JSONObject} 数据行对象
     * @param seq             {@link Supplier<Integer>} 带有序列
     * @param formulaFunction {@link Function<String:重构前的公式, String:重构后的公式>} 公式重构，替换占位符，默认将 formula 中 {column} 占位符替换为 {@link Column#name()}
     */
    public <T extends ICellWriter> T write(final T writer, final JSONObject row, final Supplier<Integer> seq, final Function<String, String> formulaFunction) {
        if (Objects.equals(type, DataType.SEQ)) {
            writer.writeNumber(seq.get());
        } else if (Objects.equals(type, DataType.FORMULA) && Objects.nonNull(formulaFunction)) {
            writer.writeFormula(formulaFunction.apply(
                    Optional.ofNullable(formula)
                            .orElse("")
                            .replaceAll("\\{column}", column.name())
            ));
        } else {
            write(writer, row);
        }
        return writer;
    }

    @Override
    public String toString() {
        return json();
    }

    public static void main(String[] args) {
        {
            CellBuilder builder = Cell.builder().label("列名").type(DataType.NUMBER).group("分组").tag("标签");
            Cell header0 = builder.index(0).build();
            Cell header1 = builder.index(1).build();
            Cell header2 = builder.index(2).build();
            log.info("{}", header0);
            log.info("{}", header1);
            log.info("{}", header2);
        }
        {
            List<Cell> headers = new ArrayList<>();
            Cell.CellBuilder builder = Cell.builder();
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
