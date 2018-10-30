package com.utils.enums;

import lombok.SneakyThrows;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * 字符编码
 *
 * @author Jason Xie on 2017/11/17.
 */
public enum Charsets {
    // UTF-8 字符编码
    UTF_8("UTF-8", Charset.forName("UTF-8")),
    GBK("GBK", Charset.forName("GBK")),
    ISO_8859_1("ISO-8859-1", Charset.forName("ISO-8859-1")),
    UTF_16BE("UTF-16BE", Charset.forName("UTF-16BE")),
    UTF_16LE("UTF-16LE", Charset.forName("UTF-16LE")),
    UTF_16("UTF-16", Charset.forName("UTF-16")),;
    public final String comment;
    public final Charset charset;

    public String displayName() {
        return charset.displayName();
    }

    /**
     * url 字符串编码
     *
     * @param value {@link String}
     * @return {@link String}
     */
    @SneakyThrows
    public String encode(final String value) {
        return URLEncoder.encode(value, charset.displayName());
    }

    /**
     * url 字符串解码
     *
     * @param value {@link String}
     * @return {@link String}
     */
    @SneakyThrows
    public String decode(final String value) {
        return URLDecoder.decode(value, charset.displayName());
    }

//    /**
//     * url 字符串编码
//     *
//     * @param value {@link String}
//     * @return {@link String}
//     */
//    public String encoder(final String value) {
//        return charset.encode(value);
//    }
//
//    /**
//     * url 字符串解码
//     *
//     * @param value {@link String}
//     * @return {@link String}
//     */
//    public String decoder(final String value) {
//
//        return ByteBuffer.wrap(value.getBytes(charset));
//    }

    Charsets(final String comment, final Charset charset) {
        this.comment = comment;
        this.charset = charset;
    }

    public static void main(String[] args) {
        System.out.println(UTF_8.encode("?json={\"name\":\"JAVA\"}"));
        System.out.println(UTF_8.encode("?json={\"name\":\"JAVA+JS\"}"));
        System.out.println(UTF_8.decode("%3Fjson%3D%7B%22name%22%3A%22JAVA%22%7D"));
    }
}
