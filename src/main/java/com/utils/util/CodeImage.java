package com.utils.util;

import com.utils.enums.Image;
import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 生成图片验证码
 *
 * @author Jason Xie on 2017/11/21.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeImage {
    /**
     * 验证码位数
     */
    @Builder.Default
    private int length = 4;
    /**
     * 验证码宽度
     */
    @Builder.Default
    private int width = 60;
    /**
     * 验证码高度
     */
    @Builder.Default
    private int height = 20;
    /**
     * 图片类型
     */
    @Builder.Default
    private Image type = Image.JPEG;
    /**
     * 图片
     */
    @Getter
    private BufferedImage image;

    /**
     * 生成图片
     *
     * @param consumer Consumer<String> 处理验证码；生成的验证码会当做参数传入 consumer
     * @return CodeImage
     */
    public CodeImage generate(@NonNull final Consumer<String> consumer) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 获取图形上下文
        final Graphics g = image.getGraphics();
        // 生成随机类
        final Random random = new Random();
        // 设定背景色
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        // 设定字体
        g.setFont(new Font("Times New Roman", Font.PLAIN,
                height / 4 + 12
//                Math.max(18, (int)(height/1.2))
        ));
        for (int i = 0; i < Math.max(80, width * height / 25); i++) {
            // 随机产生最少80条干扰线
            g.setColor(getRandColor(100, 200));
            final int x = random.nextInt(width);
            final int y = random.nextInt(height);
            final int xl = random.nextInt(width / 4 + 12);
            final int yl = random.nextInt(height / 4 + 12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        // 取随机产生的认证码
        final String code = RandomStringUtils.randomAlphanumeric(length);
        for (int i = 0; i < length; i++) {
            // 将认证码显示到图象中
            g.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
//            g.drawString(Objects.toString(code.charAt(i)), i * 17 + random.nextInt(17), 15);
            g.drawString(Objects.toString(code.charAt(i)),
                    width / length * i + random.nextInt(width / length - 12), // y = 0 则文字在左侧
                    height / 2 + random.nextInt((int) (height / 2.5)) // y = height 则文字在底部
            );
        }
        // 图象生效
        g.dispose();
        consumer.accept(code.toLowerCase());
        return this;
    }

    /*
     * 给定范围获得随机颜色
     */
    private Color getRandColor(int fc, int bc) {
        final Random random = new Random();
        if (fc > 255)
            fc = 255;
        if (bc > 255)
            bc = 255;
        final int r = fc + random.nextInt(bc - fc);
        final int g = fc + random.nextInt(bc - fc);
        final int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    @SneakyThrows
    public File write(@NonNull final File file) {
        Asserts.notEmpty(image, "请先生成图片:generate(System.out::println)");
        if (!ImageIO.write(image, type.name(), file)) {
            throw new IOException(String.format("Could not write an image of format %s to %s", type.name(), file.getAbsolutePath()));
        }
        return file;
    }

    @SneakyThrows
    public void write(@NonNull final OutputStream outputStream) {
        Asserts.notEmpty(image, "请先生成图片:generate(System.out::println)");
        if (!ImageIO.write(image, type.name(), outputStream))
            throw new IOException("Could not write an image of format ".concat(type.name()));
    }

    @SneakyThrows
    public String base64() {
        Asserts.notEmpty(image, "请先生成图片:generate(System.out::println)");
        @Cleanup final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (!ImageIO.write(image, type.name(), byteArrayOutputStream)) {
            throw new IOException("Could not write an image of format ".concat(type.name()));
        }
        return type.base64(Base64.encoder(byteArrayOutputStream.toByteArray()));
    }

    public static void main(String[] args) {
        try {
            System.out.println(
                    CodeImage.builder()
                            .type(Image.JPEG)
                            .build()
                            .generate(System.out::println)
                            .base64()
            );
            System.out.println(
                    CodeImage.builder()
                            .type(Image.PNG)
                            .build()
                            .generate(System.out::println)
                            .write(Paths.get("logs", "验证码.png").toAbsolutePath().toFile())
            );
            System.out.println(
                    CodeImage.builder()
                            .type(Image.JPEG)
                            .length(6)
                            .width(200)
                            .height(40)
                            .build()
                            .generate(System.out::println)
                            .write(Paths.get("logs", "验证码.jpeg").toAbsolutePath().toFile())
            );
            Stream.iterate(0, n->n+1).limit(100).forEach(i ->
                    System.out.println(
                            CodeImage.builder()
                                    .build()
                                    .generate(System.out::println)
                                    .write(
                                            Paths.get("logs", Util.uuid().concat(".jpeg")).toAbsolutePath().toFile()
                                    )
                                    .getAbsolutePath()
                    )
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}