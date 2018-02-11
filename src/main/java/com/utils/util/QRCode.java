package com.utils.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.utils.enums.Image;
import lombok.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Hashtable;

/**
 * 二维码生成
 *
 * @author Jason Xie on 2018/1/29.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRCode {

    /**
     * 需要生成二维码的内容
     */
    private String text;
    /**
     * 二维码宽度
     */
    @Builder.Default
    private int width = 200;
    /**
     * 二维码高度
     */
    @Builder.Default
    private int height = 200;
    /**
     * 图片类型
     */
    @Builder.Default
    private Image type = Image.JPEG;

    @SneakyThrows
    public BufferedImage generate() {
        final BitMatrix matrix = new MultiFormatWriter()
                .encode(text,
                        BarcodeFormat.QR_CODE,
                        width,
                        height,
                        new Hashtable<EncodeHintType, Object>() {{
                            put(EncodeHintType.CHARACTER_SET, "utf-8");
                            put(EncodeHintType.MARGIN, 0);
                        }}
                );
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }

    @SneakyThrows
    public File write(@NonNull final File file) {
        if (!ImageIO.write(generate(), type.name(), file)) {
            throw new IOException(String.format("Could not write an image of format %s to %s", type.name(), file.getAbsolutePath()));
        }
        return file;
    }

    @SneakyThrows
    public void write(@NonNull final OutputStream stream) {
        if (!ImageIO.write(generate(), type.name(), stream)) {
            throw new IOException("Could not write an image of format ".concat(type.name()));
        }
    }

    @SneakyThrows
    public String base64() {
        @Cleanup ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(generate(), type.name(), byteArrayOutputStream);
        return type.base64(Base64.encoder(byteArrayOutputStream.toByteArray()));
    }

    public static void main(String[] args) {
        try {
            System.out.println(
                    QRCode.builder()
                            .height(100)
                            .width(100)
                            .type(Image.JPEG)
                            .text("http://anavss.com")
                            .build()
                            .write(
                                    Paths.get("logs", "二维码.jpeg").toAbsolutePath().toFile()
                            )
                            .getAbsolutePath()
            );
            System.out.println(
                    QRCode.builder()
                            .height(100)
                            .width(100)
                            .type(Image.PNG)
                            .text("http://anavss.com")
                            .build()
                            .write(
                                    Paths.get("logs", "二维码.png").toAbsolutePath().toFile()
                            )
                            .getAbsolutePath()
            );
            System.out.println(
                    QRCode.builder()
                            .height(100)
                            .width(100)
                            .type(Image.JPEG)
                            .text("http://anavss.com")
                            .build()
                            .base64()
            );
            System.out.println(
                    QRCode.builder()
                            .height(100)
                            .width(100)
                            .type(Image.PNG)
                            .text("http://anavss.com")
                            .build()
                            .base64()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
