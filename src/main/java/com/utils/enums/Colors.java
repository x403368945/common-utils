package com.utils.enums;

import java.awt.*;

/**
 * 颜色枚举
 *
 * @author Jason Xie on 2017/11/18.
 */
public enum Colors {
    Black(new Color(0, 0, 0)),
    Brown(new Color(153, 51, 0)),
    OliveGreen(new Color(51, 51, 0)),
    DarkGreen(new Color(0, 51, 0)),
    DarkTeal(new Color(0, 51, 102)),
    DarkBlue(new Color(0, 0, 128)),
    Indigo(new Color(51, 51, 153)),
    Grey80Percent(new Color(51, 51, 51)),
    Orange(new Color(255, 102, 0)),
    DarkYellow(new Color(128, 128, 0)),
    Green(new Color(0, 128, 0)),
    Teal(new Color(0, 128, 128)),
    Blue(new Color(0, 0, 255)),
    BlueGrey(new Color(102, 102, 153)),
    Grey50Percent(new Color(128, 128, 128)),
    Red(new Color(255, 0, 0)),
    LightOrange(new Color(255, 153, 0)),
    Lime(new Color(153, 204, 0)),
    SeaGreen(new Color(51, 153, 102)),
    Aqua(new Color(51, 204, 204)),
    LightBlue(new Color(51, 102, 255)),
    Violet(new Color(128, 0, 128)),
    Grey40Percent(new Color(150, 150, 150)),
    Pink(new Color(255, 0, 255)),
    Gold(new Color(255, 204, 0)),
    Yellow(new Color(255, 255, 0)),
    BrightGreen(new Color(0, 255, 0)),
    Turquoise(new Color(0, 255, 255)),
    DarkRed(new Color(128, 0, 0)),
    SkyBlue(new Color(0, 204, 255)),
    Plum(new Color(153, 51, 102)),
    Grey25Percent(new Color(192, 192, 192)),
    Rose(new Color(255, 153, 204)),
    LightYellow(new Color(255, 255, 153)),
    LightGreen(new Color(204, 255, 204)),
    LightTurquoise(new Color(204, 255, 255)),
    PaleBlue(new Color(153, 204, 255)),
    Lavender(new Color(204, 153, 255)),
    White(new Color(255, 255, 255)),
    CornflowerBlue(new Color(153, 153, 255)),
    LemonChiffon(new Color(255, 255, 204)),
    Maroon(new Color(127, 0, 0)),
    Orchid(new Color(102, 0, 102)),
    Coral(new Color(255, 128, 128)),
    RoyalBlue(new Color(0, 102, 204)),
    LightCornflowerBlue(new Color(204, 204, 255)),
    Tan(new Color(255, 204, 153)),
    Automatic(new Color(0, 0, 0)),
    ;

    public final Color color;

    Colors(final Color color) {
        this.color = color;
    }
}
