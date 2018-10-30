package com.utils.excel.enums;

import com.utils.excel.entity.Header;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 月份
 * @author Jason Xie on 2017/10/15.
 */
public enum Month {
    // 1月
    Jan("1月"),
    Feb("2月"),
    Mar("3月"),
    Apr("4月"),
    May("5月"),
    Jun("6月"),
    Jul("7月"),
    Aug("8月"),
    Sep("9月"),
    Oct("10月"),
    Nov("11月"),
    Dec("12月"),
    ;
    /**枚举属性说明*/
    final String comment;
    Month(String comment) {
        this.comment = comment;
    }

    /**
     * 构建表格头部
     * @param headers List<Header>
     * @return List<Header>
     */
    public static List<Header> buildHeaders(List<Header> headers) {
        if (Objects.isNull(headers)) {
            headers = new ArrayList<>();
        }
        Header.HeaderBuilder builder = Header.builder().type(DataType.NUMBER);
        {
            headers.add(builder.index(headers.size()).label("1月").build());
            headers.add(builder.index(headers.size()).label("2月").build());
            headers.add(builder.index(headers.size()).label("3月").build());
        }
        {
            headers.add(builder.index(headers.size()).label("4月").build());
            headers.add(builder.index(headers.size()).label("5月").build());
            headers.add(builder.index(headers.size()).label("6月").build());
        }
        {
            headers.add(builder.index(headers.size()).label("7月").build());
            headers.add(builder.index(headers.size()).label("8月").build());
            headers.add(builder.index(headers.size()).label("9月").build());
        }
        {
            headers.add(builder.index(headers.size()).label("10月").build());
            headers.add(builder.index(headers.size()).label("11月").build());
            headers.add(builder.index(headers.size()).label("12月").build());
        }
        return headers;
    }
}
