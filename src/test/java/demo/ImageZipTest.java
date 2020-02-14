package demo;

import lombok.SneakyThrows;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;

public class ImageZipTest {
    @SneakyThrows
    public static void main(String[] args) {
        Thumbnails
                .of("/home/ccx/Desktop/src.jpg")
                .scale(0.5D)
                .outputQuality(0.5D)
                .toFile(new File("/home/ccx/Desktop/dest.jpg"))
        ;
    }
}
