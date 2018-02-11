package com.utils.util;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.utils.util.Charsets.UTF_8;

/**
 * 文件路径处理及文件对象操作
 * @author Jason Xie on 2018/1/16.
 */
@Slf4j
public class FPath {
    private Path path;
    private FPath(Path path) {
        this.path = path.toAbsolutePath();
    }
    /**
     * 格式化路径
     * @return FPath
     */
    public static FPath of(String dir, String... names) {
        return new FPath(Paths.get(dir, names));
    }
    /**
     * 格式化路径
     * @return FPath
     */
    public static FPath of(@Nonnull final File file) {
        return new FPath(file.toPath());
    }
    /**
     * 格式化路径
     * @return FPath
     */
    public static FPath of(@Nonnull final Path path) {
        return new FPath(path);
    }
    /**
     * 格式化路径
     * @param absolute String 文件绝对路径
     * @return FPath
     */
    public static FPath of(@Nonnull final String absolute) {
        return new FPath(Paths.get(absolute));
    }
    /**
     * 获取文件名
     * @return String 文件名
     */
    public String fileName() {
        return path.getFileName().toString();
    }
    /**
     * 获取文件绝对路径
     * @return String
     */
    public String absolute() {
        return path.toString();
    }
    /**
     * 获取文件对象
     * @return File
     */
    public File file() {
        return path.toFile();
    }
    /**
     * 获取文件路径对象
     * @return Path
     */
    public Path get() {
        return path;
    }
    /**
     * 是否为目录
     * @return true：目录，fasle：非目录
     */
    public boolean isDirectory() {
        return path.toFile().isDirectory();
    }
    /**
     * 文件或目录是否存在
     * @return true：存在，fasle：不存在
     */
    public boolean exist() {
        return path.toFile().exists();
    }
    /**
     * 创建目录；成功后返回file对象，便于链式调用，失败时抛出异常
     * @return File 返回File对象，便于链式调用
     */
    public FPath mkdirs(){
        File file = path.toFile();
        if(!file.exists()) {
            if (!file.mkdirs()) throw new RuntimeException("文件目录创建失败:".concat(file.getAbsolutePath()));
        }
        chmod(755);
        return this;
    }
    /**
     * 删除路径下的文件
     * @param names String[] 文件名
     */
    public void delete(final String... names) {
        delete(Arrays.asList(names));
    }
    /**
     * 删除路径下的文件
     * @param names String[] 文件名
     */
    public void delete(final List<String> names) {
        if (Util.isEmpty(names)) return;
        names.forEach(name -> {
            if (Util.isNotEmpty(name)) {
                boolean delete = path.resolve(name).toFile().delete();
                if (log.isDebugEnabled()) log.debug("删除文件【{}】{}",path.resolve(name).toString(), delete);
            }
        });
    }
    /**
     * 清除目录下的子目录及文件
     */
    public void deleteAll() {
       deleteAll(false);
    }
    /**
     * 清除文件目录
     * @param self boolean，是否清除本身：true是（递归清除完子目录和文件之后，再清除自己），false否（只清除子目录和文件）
     */
    public void deleteAll(boolean self) {
        log.info("清除目录:{}", path.toString());
        File[] files = path.toFile().listFiles();
        if (Util.isNotEmpty(files)) {
            for (File file : files) {
                if (file.isDirectory()) FPath.of(file).deleteAll(true);
                else file.delete();
            }
        }
        if (self) path.toFile().delete(); // 删除自己
    }
    /**
     * 读取文件内容
     * @return String
     */
    @SneakyThrows
    public String read() {
        { // 按字符读取文件内容不会出现乱码
            log.debug("read file：{}", path.toString());
            if (!path.toFile().exists()){
                log.warn("文件不存在：{}", path.toAbsolutePath());
                return null;
            }
            final StringBuilder sb = new StringBuilder();
            @Cleanup final BufferedReader reader = Files.newBufferedReader(path, UTF_8);
            // 一次读多个字符
            final char[] chars = new char[2048];
            int length;
            // 读入多个字符到字符数组中，count为一次读取字符数
            while ((length = reader.read(chars)) != -1) {
                sb.append(chars, 0, length);
//                log.debug(new String(chars, 0, count).replaceAll("\r\n", ""));
            }
            return sb.toString();
        }

//        { // ByteBuffer ；按字节读取，在构建String对象时可能产生乱码
//            long start = System.currentTimeMillis();
//            StringBuilder sb = new StringBuilder();
//            @Cleanup FileChannel channel = FileChannel.open(file.toPath(), EnumSet.of(StandardOpenOption.READ));
//            int allocate = 1024, length;
//            byte[] bytes = new byte[allocate];
//            ByteBuffer buffer = ByteBuffer.allocate(allocate);
//            while ((length = channel.read(buffer)) != -1) {
//                buffer.flip();
//                buffer.get(bytes, 0, length);
//                sb.append(new String(bytes, 0, length));
//                buffer.clear();
//            }
////                log.debug(sb.toString());
//            log.debug(System.currentTimeMillis() - start);
//        }
//        { // MappedByteBuffer 比 ByteBuffer快 ；按字节读取，在构建String对象时可能产生乱码
//            long start = System.currentTimeMillis();
//            StringBuilder sb = new StringBuilder();
//            @Cleanup FileChannel channel = FileChannel.open(file.toPath(), EnumSet.of(StandardOpenOption.READ));
//            int allocate = 1024, count = (int) channel.size() / allocate, mode = (int) channel.size() % allocate;
//            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
//            byte[] bytes = new byte[allocate];
//            for (int i = 0; i < count; i++) {
//                buffer.get(bytes);
//                sb.append(new String(bytes));
//            }
//            if (mode > 0) {
//                buffer.get(bytes, 0, mode);
//                sb.append(new String(bytes, 0, mode));
//            }
////                log.debug(sb.toString());
//            log.debug(System.currentTimeMillis() - start);
//        }
    }
    /**
     * 设置文件权限
     */
    public FPath chmod() {
        try {
            if (isDirectory()) Runtime.getRuntime().exec("chmod 755 ".concat(absolute()));
            else Runtime.getRuntime().exec("chmod 644 ".concat(absolute()));
        } catch (Exception e) {
        }
        return this;
    }
    /**
     * 设置文件权限
     * @param value int
     */
    public FPath chmod(final int value) {
        try {
           Runtime.getRuntime().exec(String.format("chmod %d %s", value, absolute()));
        } catch (Exception e) {
        }
        return this;
    }
    /**
     * 文件名处理
     */
    public static class FileName {
        private String name;
        private FileName(String name) {
            this.name = name;
        }
        public static FileName of(final String filename) {
            Asserts.notEmpty(filename, "参数【filename】是必须的");
            return new FileName(filename);
        }
        /**
         * 获取文件后缀名,带"."
         * @return String
         */
        public String getSubfix() {
            return getSubfix(true);
        }
        /**
         * 获取文件后缀名，可选择是否带点；例：test.txt，带点则返回：.txt，不带点则返回：txt
         * @param offset 是否带.,true：带点.，false不带点.
         * @return String
         */
        public String getSubfix(boolean offset) {
            return name.replaceFirst("^.+\\.", (offset ? "." : ""));
        }
        /**
         * 获取文件名，不带后缀
         * @return String
         */
        public String getPrefix() {
            return name.substring(0, name.lastIndexOf("."));
        }
        /**
         * 文件名转换为UUID文件名
         * @return String UUID文件名，带后缀
         */
        public String getUuidFileName() {
            return Util.uuid() + getSubfix();
        }
    }

    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        log.debug("{}",runtime.maxMemory());
        log.debug("{}",runtime.totalMemory());
        log.debug("{}",runtime.freeMemory());
        java.nio.file.Path path = Paths.get("src/test/files/temp");
        log.debug("{}", path.resolve(".zip"));
        log.debug(path.getParent().toString());
        log.debug(path.getName(path.getNameCount() - 1) + ".zip");
        log.debug(Paths.get(path.getParent().toString(), path.getFileName() + ".zip").toAbsolutePath().toString());
        try {
            Dates date = Dates.now();
            System.out.println(
                    FPath
                            .of("src/test/files/json/test.json")
                            .read()
            );
            log.debug("{}", date.getTimeConsuming());
        } catch (Exception e) {
            e.printStackTrace();
        }
//

    }
}
