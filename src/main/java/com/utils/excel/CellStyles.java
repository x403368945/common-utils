package com.utils.excel;

import lombok.*;
import lombok.experimental.Accessors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.awt.Color;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

/**
 * 单元格样式
 *
 * @author Jason Xie on 2018-8-8.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Accessors(chain = true)
public class CellStyles
//        implements CellStyle
{
    /**
     * 样式索引
     */
    private Short index;
    private Short dataFormat;
    private Font font;
    private Boolean hidden;
    private Boolean locked;
    private Boolean quotePrefixed;
    private HorizontalAlignment alignment;
    private Boolean wrapText;
    private VerticalAlignment verticalAlignment;
    private Short rotation;
    private Short indention;
    private BorderStyle borderLeft;
    private BorderStyle borderRight;
    private BorderStyle borderTop;
    private BorderStyle borderBottom;
    private IndexedColors leftBorderColor;
    private IndexedColors rightBorderColor;
    private IndexedColors topBorderColor;
    private IndexedColors bottomBorderColor;
    /**
     * 填充模式
     */
    private FillPatternType fillPattern;
    /**
     * 背景颜色
     */
    private Color fillBackgroundColor;
    /**
     * 前景颜色，单元格的颜色都是设置前景颜色，背景颜色设置无效；设置前景颜色之前需要设置填充模式（fillPattern）
     */
    private Color fillForegroundColor;
    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ 实现 CellStyle 接口必须实现的方法，有一部分可以通过 @data实现，剩下这些不能实现的
//    @Override
//    public String getDataFormatString() {
//        return null;
//    }
//    @Override
//    public short getFontIndex() {
//        return font.getIndex();
//    }
//    @Override
//    public boolean getHidden() {
//        return hidden;
//    }
//    @Override
//    public boolean getLocked() {
//        return locked;
//    }
//    @Override
//    public boolean getQuotePrefixed() {
//        return quotePrefixed;
//    }
//    @Override
//    public short getAlignment() {
//        return alignment.getCode();
//    }
//    @Override
//    public HorizontalAlignment getAlignmentEnum() {
//        return alignment;
//    }
//    @Override
//    public boolean getWrapText() {
//        return wrapText;
//    }
//    @Override
//    public short getVerticalAlignment() {
//        return verticalAlignment.getCode();
//    }
//    @Override
//    public VerticalAlignment getVerticalAlignmentEnum() {
//        return verticalAlignment;
//    }
//    @Override
//    public short getBorderLeft() {
//        return borderLeft.getCode();
//    }
//    @Override
//    public BorderStyle getBorderLeftEnum() {
//        return borderLeft;
//    }
//    @Override
//    public short getBorderRight() {
//        return borderRight.getCode();
//    }
//    @Override
//    public BorderStyle getBorderRightEnum() {
//        return borderRight;
//    }
//    @Override
//    public short getBorderTop() {
//        return borderTop.getCode();
//    }
//    @Override
//    public BorderStyle getBorderTopEnum() {
//        return borderTop;
//    }
//    @Override
//    public short getBorderBottom() {
//        return borderBottom.getCode();
//    }
//    @Override
//    public BorderStyle getBorderBottomEnum() {
//        return borderBottom;
//    }
//    @Override
//    public short getFillPattern() {
//        return fillPattern.getCode();
//    }
//    @Override
//    public FillPatternType getFillPatternEnum() {
//        return fillPattern;
//    }
//    @Override
//    public Color getFillBackgroundColorColor() {
//        return null;
//    }
//    @Override
//    public Color getFillForegroundColorColor() {
//        return null;
//    }
//    @Override
//    public void cloneStyleFrom(CellStyle cellStyle) {}
//    @Override
//    public void setShrinkToFit(boolean b) {}
//    @Override
//    public boolean getShrinkToFit() {
//        return false;
//    }
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ 实现 CellStyle 接口必须实现的方法，有一部分可以通过 @data实现，剩下这些不能实现的

    /**
     * 克隆追加样式缓存
     */
    private final HashMap<Short, CellStyle> CACHE = new HashMap<>();

    /**
     * 创建并缓存 CellStyle
     *
     * @param workbook Workbook 指定样式库
     * @return CellStyle
     */
    public CellStyle createCellStyle(final Workbook workbook) {
        // Short.MIN_VALUE 存储初始化时默认缓存样式
        return Optional
                .ofNullable(CACHE.get(Short.MIN_VALUE))
                .orElseGet(() -> {
                    CACHE.put(Short.MIN_VALUE, append(workbook.createCellStyle())); // 若样式没有缓存，则创建并缓存，便于样式复用
                    return CACHE.get(Short.MIN_VALUE);
                });
    }

    /**
     * 克隆 fromStyle 并追加样式；若参数 fromStyle 为空，则创建并返回默认样式（存储在 Short.MIN_VALUE 中）
     *
     * @param workbook  Workbook 指定样式库
     * @param fromStyle CellStyle
     * @return CellStyle
     */
    public CellStyle appendClone(final Workbook workbook, final CellStyle fromStyle) {
        if (Objects.isNull(fromStyle)) return createCellStyle(workbook);
        return Optional
                .ofNullable(CACHE.get(fromStyle.getIndex()))
                .orElseGet(() -> {
                    final CellStyle style = workbook.createCellStyle();
                    style.cloneStyleFrom(fromStyle);
                    append(style);
                    CACHE.put(fromStyle.getIndex(), style); // 缓存被克隆的样式，当下次克隆，直接获取缓存
                    CACHE.put(style.getIndex(), style); // 缓存克隆并追加后的样式，当使用此样式继续克隆是无效的（本身无变化，直接取缓存即可）
                    return style;
                });
    }

    /**
     * 追加样式，必须先指定样式；若参数 style 为空，则返回null
     * 警告：单独追加样式会导致所有引用此样式的单元格发生改变，若不确定情况下请使用 appendClone 方法
     *
     * @param style CellStyle
     * @return CellStyle
     */
    public CellStyle append(final CellStyle style) {
        XSSFCellStyle cellStyle = (XSSFCellStyle) style;
        if (Objects.isNull(cellStyle)) return null;
        if (Objects.nonNull(dataFormat)) cellStyle.setDataFormat(dataFormat);
        if (Objects.nonNull(font)) cellStyle.setFont(font);
        if (Objects.nonNull(hidden)) cellStyle.setHidden(hidden);
        if (Objects.nonNull(locked)) cellStyle.setLocked(locked);
        if (Objects.nonNull(quotePrefixed)) cellStyle.setQuotePrefixed(quotePrefixed);
        if (Objects.nonNull(alignment)) cellStyle.setAlignment(alignment);
        if (Objects.nonNull(wrapText)) cellStyle.setWrapText(wrapText);
        if (Objects.nonNull(verticalAlignment)) cellStyle.setVerticalAlignment(verticalAlignment);
        if (Objects.nonNull(rotation)) cellStyle.setRotation(rotation);
        if (Objects.nonNull(indention)) cellStyle.setIndention(indention);
        if (Objects.nonNull(borderLeft)) cellStyle.setBorderLeft(borderLeft);
        if (Objects.nonNull(borderRight)) cellStyle.setBorderRight(borderRight);
        if (Objects.nonNull(borderTop)) cellStyle.setBorderTop(borderTop);
        if (Objects.nonNull(borderBottom)) cellStyle.setBorderBottom(borderBottom);
        if (Objects.nonNull(leftBorderColor)) cellStyle.setLeftBorderColor(leftBorderColor.index);
        if (Objects.nonNull(rightBorderColor)) cellStyle.setRightBorderColor(rightBorderColor.index);
        if (Objects.nonNull(topBorderColor)) cellStyle.setTopBorderColor(topBorderColor.index);
        if (Objects.nonNull(bottomBorderColor)) cellStyle.setBottomBorderColor(bottomBorderColor.index);
        if (Objects.nonNull(fillPattern)) cellStyle.setFillPattern(fillPattern);
        if (Objects.nonNull(fillBackgroundColor)) cellStyle.setFillBackgroundColor(new XSSFColor(fillBackgroundColor, new DefaultIndexedColorMap()));
        if (Objects.nonNull(fillForegroundColor)) cellStyle.setFillForegroundColor(new XSSFColor(fillForegroundColor, new DefaultIndexedColorMap()));
        return cellStyle;
    }
//    /**
//     * 克隆 fromStyle 样式；若参数 fromStyle 为空，则返回 null
//     * @param workbook Workbook 指定样式库
//     * @param fromStyle CellStyle
//     * @return CellStyle
//     */
//    public CellStyle clone(final Workbook workbook, final CellStyle fromStyle) {
//        if(CACHE_CLONE.containsKey(fromStyle.getIndex())) {
//            return CACHE_CLONE.get(fromStyle.getIndex());
//        } else {
//            CellStyle style = workbook.createCellStyle();
//            style.cloneStyleFrom(fromStyle);
//            CACHE_CLONE.put(fromStyle.getIndex(), style); // 缓存被克隆的样式，当下次克隆，直接获取缓存
//            CACHE_CLONE.put(style.getIndex(), style); // 缓存克隆并追加后的样式，当使用此样式继续克隆是无效的（本身无变化，直接取缓存就行）
//            return style;
//        }
//    }

}