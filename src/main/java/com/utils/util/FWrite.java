package com.utils.util;

import com.alibaba.fastjson.JSON;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Optional;

import static java.nio.file.StandardOpenOption.APPEND;

/**
 * 文件复制操作
 * 注：copy() 方法不支持指定目标文件绝对路径；即目标永远是目录，而不是确切的文件名；若要复制到指定目标文件，请调用copyTo() 方法指定
 *
 * @author Jason Xie on 2017/10/30.
 */
@Slf4j
public class FWrite {
    public static FWrite of(File file) {
        return new FWrite(file);
    }

    public static FWrite of(String path, String... names) {
        return of(FPath.of(path, names).file());
    }

    private FWrite(File file) {
        this.file = file;
    }

    /**
     * 写入路径：绝对路径
     */
    @Getter
    private File file;
    /**
     * 文件存在时追加到尾部,true:追加，false不追加
     */
    private boolean isAppend;

    public FWrite append() {
        this.isAppend = true;
        return this;
    }

    public Optional<String> getAbsolute() {
        return Objects.isNull(file) ? Optional.empty() : Optional.of(file.getAbsolutePath());
    }

    public Optional<String> getFileName() {
        return Objects.isNull(file) ? Optional.empty() : Optional.of(file.getName());
    }

    @SneakyThrows
    private void check() {
        log.debug("write file : {}", file.getAbsolutePath());
        Asserts.notEmpty(file, "请指定写入路径");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
            FPath.of(file.getParentFile()).chmod(755);
        }
        if (!file.exists()) file.createNewFile();
    }

    @SneakyThrows
    public FWrite write(final String content) {
//        check();
//        @Cleanup FileOutputStream fileOutputStream = new FileOutputStream(file, isAppend);
//        @Cleanup OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
//        if (Objects.isNull(content)) {
//            log.warn("本次写入内容为空:{}", file.getAbsolutePath());
//        } else {
//            outputStreamWriter.write(content);
//        }
//        outputStreamWriter.flush();
//        FPath.of(file).chmod(644);
        write(content.getBytes(Charsets.UTF_8));
        return this;
    }

    public FWrite writeJson(Object obj) {
        write(JSON.toJSONString(obj).getBytes(Charsets.UTF_8));
        return this;
    }

    @SneakyThrows
    public FWrite write(final byte[] content) {
        check();
//        @Cleanup FileOutputStream fileOutputStream = new FileOutputStream(file, isAppend);
//        @Cleanup BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
//        if (Objects.isNull(content)) {
//            log.warn("本次写入内容为空:" + file.getAbsolutePath());
//        } else {
//            bos.write(content);
//        }
//        bos.flush();
//        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.APPEND
        if (isAppend)
            Files.write(
                    file.toPath(),
                    content,
                    file.exists()
                            ? new StandardOpenOption[]{APPEND}
                            : new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING}
            );
        else
            Files.write(file.toPath(), content);

        FPath.of(file.getParentFile()).chmod(755);
        FPath.of(file).chmod(644);
        return this;
    }

    public static void main(String[] args) {
        try {
            log.debug("{}", FWrite.of("logs", "test.txt")
                    .write("aaa")
                    .getAbsolute()
                    .get());
            log.debug("{}", FWrite.of("logs", "test.txt")
                    .append()
                    .write("bbb")
                    .getAbsolute()
                    .get());
            log.debug("{}", FWrite.of("logs", "test.txt")
                    .append()
                    .write("ccc".getBytes(Charsets.UTF_8))
                    .getAbsolute()
                    .get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
