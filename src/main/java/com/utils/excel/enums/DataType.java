package com.utils.excel.enums;

import com.utils.util.Dates;
import com.utils.util.FWrite;
import lombok.val;
import org.apache.poi.ss.usermodel.CellType;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * EXCEL数据类型定义
 * @author 谢长春 on 2018-10-3
 */
public enum DataType {
    /** 数值*/
    NUMBER("数值", CellType.NUMERIC),
    /** 文本*/
    TEXT("文本", CellType.STRING),
    /** 日期*/
    DATE("日期", CellType.NUMERIC),
    /** 百分比*/
    PERCENT("百分比", CellType.NUMERIC),
    ;
    /**
     * 枚举属性说明
     */
    final String comment;
    public final CellType cellType;

    DataType(String comment, CellType cellType) {
        this.comment = comment;
        this.cellType = cellType;
    }
    public static void main(String[] args) {
        { // 构建 js 枚举文件
            val name = "枚举：EXCEL数据类型定义";
            val className = DataType.class.getSimpleName();
            StringBuilder sb = new StringBuilder();
            sb.append("/**\n")
                    .append(" * ").append(name).append("\n")
                    .append(String.format(" * Created by 谢长春 on %s.\n", Dates.now().formatDate()))
                    .append(" */\n");
            sb.append("// 枚举值定义").append("\n");
            sb.append(String.format("const %s = Object.freeze({", className)).append("\n");
            Stream.of(DataType.values()).forEach(item -> sb.append(
                    "\t{name}: {value: '{name}', comment: '{comment}'},"
                            .replace("{name}", item.name())
                            .replace("{comment}", item.comment)
                    ).append("\n")
            );
            sb.append("});").append("\n");
            sb.append("// 枚举值转换为选项集合").append("\n");
            sb.append(String.format("export const %sOptions = [", className)).append("\n");
            Stream.of(DataType.values()).forEach(item -> sb.append(
                    "\t{value: {class}.{name}.value, label: {class}.{name}.comment},"
                            .replace("{class}", className)
                            .replace("{name}", item.name())
                    ).append("\n")
            );
            sb.append("];").append("\n");
            sb.append("export default {class};".replace("{class}", className));
            System.out.println(
                    "JS文件输出路径：\n" +
                            FWrite.of("logs", className.concat(".js")).write(sb.toString()).getAbsolute().orElse(null));
        }

        System.out.println(
                Stream.of(DataType.values())
                        .map(item -> String.format("%s【%s】", item.name(), item.comment))
                        .collect(Collectors.joining("|"))
        );
    }
}
