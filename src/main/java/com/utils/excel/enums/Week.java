package com.utils.excel.enums;

import com.alibaba.fastjson.JSON;
import com.utils.util.Dates;
import com.utils.util.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

/**
 * 枚举：星期
 *
 * @author Jason Xie on 2017/11/1.
 */
@Slf4j
public enum Week {
    Sun("星期日", "Sunday"),
    Mon("星期一", "Monday"),
    Tue("星期二", "Tuesday"),
    Wed("星期三", "Wednesday"),
    Thu("星期四", "Thursday"),
    Fri("星期五", "Friday"),
    Sat("星期六", "Saturday");
    final String zh;
    final String en;

    public String value() {
        return zh;
    }

    public String zh() {
        return zh;
    }

    public String en() {
        return en;
    }

    public String json() {
        return JSON.toJSONString(Maps.ofSS().put("zh_CN", zh).put("en_US", en).build());
    }

    Week(final String zh, final String en) {
        this.zh = zh;
        this.en = en;
    }

    public static void main(String[] args) {
        log.info(Week.Mon.name());
        log.info(Week.Mon.json());
        Dates before = Dates.now().addDay(-6);
        log.info("输出之前一周");
        for (int i = 0; i < 7; i++) {
            log.info(before.formatDate() + ": " + before.week() + " :" + before.week().json());
            before.addDay(1);
        }
        log.info("输出之后一周");
        Dates after = Dates.now();
        Stream.iterate(0, n -> n + 1)
                .limit(7)
                .forEach(n -> {
                    log.info(after.formatDate() + ": " + after.week() + " :" + after.week().json());
                    after.addDay(1);
                });
    }
}
