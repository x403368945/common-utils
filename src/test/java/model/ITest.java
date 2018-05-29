//package model;
//
//import com.utils.util.FWrite;
//import org.springframework.test.web.servlet.ResultActions;
//
//import javax.annotation.Nonnull;
//import java.io.IOException;
//import java.nio.file.Paths;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * @author Jason Xie on 2017/11/20.
// */
//public interface ITest {
//    /**
//     * 获取日志存储路径
//     */
//    default java.nio.file.Path getPath() {
//        return Paths.get("logs").toAbsolutePath().resolve(this.getClass().getSimpleName()); // 单元测试结果，写入文件路径
//    }
//
//    default String getMethodName() {
//        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
//        Matcher m = Pattern.compile("(?<=[$])(.+)(?=[$])").matcher(methodName);
//        return m.find() ? m.group() : methodName;
//    }
//
//    default String format(String pattern, Object... values) {
//        for (Object value : values) {
//            pattern = pattern.replaceFirst("\\{([a-zA-Z0-9]+)?}", value.toString());
//        }
//        return pattern;
//    }
//
//    default void asserts(@Nonnull String methodName, @Nonnull ResultActions resultActions) {
//        Tester.builder()
//                .clazz(this.getClass())
//                .methodName(methodName)
//                .build()
//                .asserts(resultActions);
//    }
//
//    default void writeFile(String methodName, String content) throws IOException {
//        try {
//            System.err.println(
//                    FWrite.of(getPath().resolve(methodName + ".json").toFile())
//                            .write(
//                                    content
//                            )
//                            .getAbsolute()
//                            .orElse(null)
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}