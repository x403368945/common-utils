package com.utils.common.entity.excel;

import com.alibaba.fastjson.annotation.JSONField;
import com.utils.common.entity.IJson;
import com.utils.enums.DataType;
import com.utils.util.Dates;
import com.utils.util.Num;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * excel 解析单元格实体
 *
 * @author Jason Xie on 2017/10/19.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Accessors(chain = true)
public class Cell implements IJson {

    /**
     * 单元格数据类型
     */
    private DataType type;
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
     * 单元格样式索引
     */
    private Integer sindex;

    /**
     * 将 value 转换为数字处理对象
     * 警告：value 可能为null，这里将返回绝对的数字处理对象，当 value 为 null 时，数字处理对象中的 value 也为null
     *
     * @return Num
     */
    @JSONField(serialize = false, deserialize = false)
    public Num getNumber() {
        return Num.of(value);
    }

    /**
     * 将 value 转换为日期处理对象
     *
     * @return Dates
     */
    @JSONField(serialize = false, deserialize = false)
    public Dates getDate() {
        return Num.of(value).toDate();
    }

    @Override
    public String toString() {
        return json();
    }
}
