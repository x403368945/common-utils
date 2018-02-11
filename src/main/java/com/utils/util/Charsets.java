package com.utils.util;

import java.nio.charset.Charset;

/**
 * 字符编码
 * @author Jason Xie on 2017/11/17.
 */
public final class Charsets {
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Charset UTF_16BE = Charset.forName("UTF-16BE");
    public static final Charset UTF_16LE = Charset.forName("UTF-16LE");
    public static final Charset UTF_16 = Charset.forName("UTF-16");
    public static final Charset GBK = Charset.forName("GBK");

    private Charsets() {
    }

    public static void main(String[] args) {
        System.out.println(UTF_8.name());
        System.out.println(UTF_8.displayName());
        System.out.println(UTF_8.encode("测试aaa"));
        System.out.println(UTF_8.decode(UTF_8.encode("测试aaa")));
    }

}
