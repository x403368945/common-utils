package com.utils.excel.enums;

import com.utils.excel.entity.Header;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 季度
 * @author 谢长春 on 2017/10/15 .
 */
public enum Quarter {
    // 第一季度
    ONE("第一季度"),
    TWO("第二季度"),
    THREE("第三季度"),
    FOUR("第四季度"),
    ;
    /**枚举属性说明*/
    final String comment;
    Quarter(String comment) {
        this.comment = comment;
    }

    /**
     * 按季度分组构建表格头部
     * @param headers {@link List}{@link List<Header>}
     * @return {@link List}{@link List<Header>}
     */
    public static List<Header> buildHeaders(List<Header> headers) {
        if (Objects.isNull(headers)) {
            headers = new ArrayList<>();
        }
        return buildHeaders(headers, headers.size());
    }
    /**
     * 按季度分组构建表格头部
     * @param headers {@link List}{@link List<Header>}
     * @param startIndex int index 起始值
     * @return {@link List}{@link List<Header>}
     */
    public static List<Header> buildHeaders(List<Header> headers, int startIndex) {
        if (Objects.isNull(headers)) {
            headers = new ArrayList<>();
        }
        Header.HeaderBuilder builder = Header.builder().type(DataType.NUMBER);
        {
            builder.group(ONE.comment);
            headers.add(builder.index(startIndex++).label("1月").build());
            headers.add(builder.index(startIndex++).label("2月").build());
            headers.add(builder.index(startIndex++).label("3月").build());
            headers.add(builder.index(startIndex++).label("合计").build());
        }
        {
            builder.group(TWO.comment);
            headers.add(builder.index(startIndex++).label("4月").build());
            headers.add(builder.index(startIndex++).label("5月").build());
            headers.add(builder.index(startIndex++).label("6月").build());
            headers.add(builder.index(startIndex++).label("合计").build());
        }
        {
            builder.group(THREE.comment);
            headers.add(builder.index(startIndex++).label("7月").build());
            headers.add(builder.index(startIndex++).label("8月").build());
            headers.add(builder.index(startIndex++).label("9月").build());
            headers.add(builder.index(startIndex++).label("合计").build());
        }
        {
            builder.group(FOUR.comment);
            headers.add(builder.index(startIndex++).label("10月").build());
            headers.add(builder.index(startIndex++).label("11月").build());
            headers.add(builder.index(startIndex++).label("12月").build());
            headers.add(builder.index(startIndex++).label("合计").build());
        }
        return headers;
    }
}
