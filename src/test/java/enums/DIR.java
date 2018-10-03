package enums;

import com.alibaba.fastjson.JSON;
import com.utils.util.FPath;
import com.utils.util.FWrite;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * 单元测试文件定义目录
 *
 * @author Jason Xie on 2017/12/13.
 */
public enum DIR {
    ROOT("项目根目录", Paths.get("").toAbsolutePath().toString()),
    FILES("test/files目录", ROOT.absolute("src/test/files")),
    TEMP("文件上传及临时文件存储目录", FILES.absolute("temp")),;
    /**
     * 枚举属性说明
     */
    final String comment;
    final String path;

    DIR(String comment, String path) {
        this.comment = comment;
        this.path = path;
    }

    /**
     * 获取文件路径操作对象
     *
     * @param names {@link String[]} 追加目录名或文件名
     * @return {@link FPath}
     */
    public FPath fpath(String... names) {
        return FPath.of(this.path, names);
    }

    /**
     * 获取绝对路径
     *
     * @param names String[] 追加目录名或文件名
     * @return String 文件绝对路径：d:\java
     */
    public String absolute(String... names) {
        return FPath.of(this.path, names).absolute();
    }

    /**
     * 获取文件对象
     *
     * @param names String[] 追加目录名或文件名
     * @return File 文件对象
     */
    public File file(String... names) {
        return FPath.of(this.path, names).file();
    }

    /**
     * 读取文件内容
     *
     * @param names String[] 追加目录名或文件名
     * @return File 文件对象
     * @throws IOException 文件读取异常
     */
    public String read(String... names) throws IOException {
        return FPath.of(this.path, names).read();
    }

    /**
     * 写入文本到文件
     *
     * @param content String 写入的文件内容
     * @param names   String[] 追加目录名及文件名
     * @return File 文件对象
     * @throws IOException 文件读取异常
     */
    public String write(String content, String... names) throws IOException {
        return FWrite.of(absolute(names)).write(content).getAbsolute().orElse(null);
    }

    public static void main(String[] args) {
        for (DIR dir : DIR.values()) {
            System.out.println(dir.absolute());
        }
    }
}
