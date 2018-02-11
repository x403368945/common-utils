package com.utils.util;

import lombok.Cleanup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件或目录压缩操作 <br>
 * 可以不指定 to ，会默认在 from 同级的目录下产生一个同名的压缩包； <br>
 * 例1：当 from = D:\files\dir ；则默认 to = D:\files\dir.zip <br>
 * 例2：当 from = D:\files\content.txt ；则默认 to = D:\files\content.zip <br>
 * @author Jason Xie on 2017/10/30.
 */
@Slf4j
public class FZip {
    public static FZip build() {
        return new FZip();
    }
    /**
     * 源文件或目录：绝对路径
     */
    @Getter
    private File from;
    /**
     * 目标文件：绝对路径
     */
    @Getter
    private File to;
    /**
     * 是否递归压缩子目录, true:是，false：否
     */
    private boolean isRecursion = true;
    /**
     * Consumer<Integer> 压缩进度回调
     */
    private Consumer<Integer> progress;
    /**
     * 设置不包含文件的正则表达式
     */
    private Predicate<String> exclude;

    public FZip from(File from) {
        this.from = from;
        return this;
    }
    public FZip from(String from, String... names) {
        return from(FPath.of(from, names).file());
    }

    public FZip to(File to) {
        this.to = to;
        return this;
    }
    public FZip to(String to, String... names) {
        return to(FPath.of(to, names).file());
    }

    public FZip recursion(boolean recursion) {
        isRecursion = recursion;
        return this;
    }

    /**
     * 指定压缩进度回调
     * @param progress Consumer<Integer>
     * @return FZip
     */
    public FZip progress(Consumer<Integer> progress) {
        this.progress = progress;
        return this;
    }
    public FZip exclude(String regex) {
        this.exclude = Pattern.compile(regex).asPredicate();
        return this;
    }

    public String getFromFileName() {
        return from.getName();
    }

    public String getToFileName() {
        return to.getName();
    }

    /**
     * 执行压缩操作
     * @return Zip
     */
    public FZip zip() throws Exception{
        { // 检查参数是否正确
            Asserts.notNull(from, "文件或目录不存在:".concat(from.getAbsolutePath()));
            Asserts.isTrue(from.exists(), "文件或目录不存在:".concat(from.getAbsolutePath()));
            if (Util.isEmpty(to)) {
                to = from.getParentFile()
                        .toPath()
                        .resolve(
                                (from.isDirectory() ? getFromFileName() : FPath.FileName.of(getFromFileName()).getPrefix()).concat(".zip")
                        )
                        .toFile();
            } else {
                Asserts.isTrue(to.getName().endsWith(".zip"), "目标后缀必须是 .zip 的文件，不能是目录或其他后缀:".concat(to.getAbsolutePath()));
            }
        }
//        Dates dates = Dates.now();
        final File[] files = from.isDirectory()
                ? from.listFiles((dir, name) -> Objects.isNull(exclude) || !exclude.test(name))
                : new File[]{from};
        Asserts.notNull(files, "压缩目录文件列表为空");
        @Cleanup final BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(to.toPath()));
        @Cleanup final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        int p = 0; // 进度
        for (int i = 0; i < files.length; i++) {
            write(files[i], zipOutputStream, Paths.get(""));
            if (Objects.nonNull(progress)) {
                if((int)((i + 1.0) / files.length * 100) > p) {
                    p = (int)((i + 1.0) / files.length * 100);
                    progress.accept(p);
                }
            }
        }
        zipOutputStream.finish();
        zipOutputStream.flush();
//        System.out.println(dates.getTimeConsuming());
        FPath.of(to.getParentFile()).chmod(755);
        FPath.of(to).chmod(644);
        return this;
    }
    private void write(final File source, final ZipOutputStream output, final Path parent) throws Exception {
        if (source.isDirectory()) {
            if (isRecursion)
                for (Path path : Files.newDirectoryStream(source.toPath()))
                    write(path.toFile(), output, parent.resolve(source.getName()));
        } else {
            @Cleanup final FileChannel channel = FileChannel.open(source.toPath(), StandardOpenOption.READ);
            final ByteBuffer buffer = ByteBuffer.allocate(2048);
            output.putNextEntry(new ZipEntry(parent.resolve(source.getName()).toString()));
            int length;
            while (-1 != (length = channel.read(buffer))) {
                buffer.flip();
                output.write(buffer.array(), 0, length);
                buffer.clear();
            }
            output.closeEntry();
        }
    }


    public static void main(String[] args) {
        try {
            FZip zip = FZip.build()
                    .from("src/test/files/json")
                    .to("src/test/files/temp/json.zip")
                    .zip();
            log.debug(zip.getTo().getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}