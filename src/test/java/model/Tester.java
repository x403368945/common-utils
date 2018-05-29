//package model;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import com.utils.common.entity.base.ParamsInfo;
//import com.utils.common.entity.base.ResultInfo;
//import com.utils.util.FWrite;
//import lombok.Builder;
//import lombok.Data;
//import lombok.experimental.Accessors;
//import org.springframework.test.util.AssertionErrors;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.ResultActions;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//
//import java.net.URLEncoder;
//import java.nio.file.Paths;
//import java.util.Objects;
//import java.util.function.Consumer;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import static com.utils.enums.Code.SUCCESS;
//
///**
// * @author Jason Xie on 2017/11/21.
// */
//@Builder
//@Data
//@Accessors(chain = true, fluent = true)
//public class Tester {
//    private Class clazz;
//    private String methodName;
//    private MockMvc mockMvc;
//    private String url;
//    private String response;
//
//    /**
//     * 获取日志存储路径
//     */
//    public java.nio.file.Path getPath() {
//        return Paths.get("logs").toAbsolutePath().resolve(clazz.getSimpleName()); // 单元测试结果，写入文件路径
//    }
//
//    public String getMethodName() {
//        if(Objects.isNull(this.methodName)) {
//            this.methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
//            Matcher m = Pattern.compile("(?<=[$])(.+)(?=[$])").matcher(this.methodName);
//            if(m.find()) {
//                this.methodName = m.group();
//            }
//        }
//        return this.methodName;
//    }
//
//    public void asserts(ResultActions resultActions) {
//        try {
//            resultActions
//                    .andExpect(MockMvcResultMatchers.status().isOk())
//                    .andDo(MockMvcResultHandlers.print())
//                    .andExpect(res -> {
//                        this.response = res.getResponse().getContentAsString();
//                        ResultInfo<Object> resultInfo = ResultInfo.ofJson(response);
//                        writeFile(response);
//                        AssertionErrors.assertEquals("code", SUCCESS, resultInfo.getCode());
//                    })
//            ;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void writeFile(final String content) {
//        try {
//            System.err.println(
//                    FWrite.of(getPath().resolve(getMethodName() + ".json").toFile())
//                            .write(
//                                    JSON.toJSONString(
//                                            ResultInfo.ofJson(content)
//                                            , SerializerFeature.PrettyFormat
//                                    )
//                            )
//                            .getAbsolute()
//                            .orElse(null)
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private String format(String pattern, Object... values) {
//        for (Object value : values) {
//            pattern = pattern.replaceFirst("\\{([a-zA-Z0-9]+)?}", value.toString());
//        }
//        return pattern;
//    }
//
//    public Tester formatResponse(Consumer<String> consumer) {
//        consumer.accept(response);
//        return this;
//    }
//
//    public Tester post(Object params) {
//        try {
//            System.err.println("url:\n"+url);
//            System.err.println("参数:\n"+new ParamsInfo(params).toString());
//            asserts(
//                    mockMvc.perform(
//                            MockMvcRequestBuilders
//                                    .post(url)
//                                    .content(new ParamsInfo(params).toString())
//                    )
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return this;
//    }
//
//    public Tester put(Object params) {
//        try {
//            System.err.println("url:\n"+url);
//            System.err.println("参数:\n"+new ParamsInfo(params).toString());
//            asserts(
//                    mockMvc.perform(
//                            MockMvcRequestBuilders
//                                    .put(url)
//                                    .content(new ParamsInfo(params).toString())
//                    )
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return this;
//    }
//
//    public Tester patch(Object params) {
//        try {
//            System.err.println("url:\n"+url);
//            System.err.println("参数:\n"+new ParamsInfo(params).toString());
//            asserts(
//                    mockMvc.perform(
//                            MockMvcRequestBuilders
//                                    .patch(url)
//                                    .content(new ParamsInfo(params).toString())
//                    )
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return this;
//    }
//
//    public Tester delete(Object params) {
//        try {
//            System.err.println("url:\n"+url);
//            System.err.println("参数:\n"+new ParamsInfo(params).toString());
//            asserts(
//                    mockMvc.perform(
//                            MockMvcRequestBuilders
//                                    .delete(url)
//                                    .content(new ParamsInfo(params).toString())
//                    )
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return this;
//    }
//
//    public Tester get(Object params) {
//        try {
//            System.err.println("url:\n"+url);
//            System.err.println("参数:\n"+new ParamsInfo(params).toString());
//            asserts(
//                    mockMvc.perform(
//                            MockMvcRequestBuilders
//                                    .get(url)
//                                    .param("params", JSON.toJSONString(params))
//                    )
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return this;
//    }
//
//    public Tester updateById(String id, Object params) {
//        try {
//            System.err.println("url:\n"+format(url + "/{id}", id));
//            System.err.println("参数:\n"+new ParamsInfo(params).toString());
//            asserts(
//                    mockMvc.perform(
//                            MockMvcRequestBuilders
//                                    .put(url + "/{id}", id)
//                                    .content(new ParamsInfo(params).toString())
//                    )
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return this;
//    }
//
//    public Tester deleteById(String id) {
//        try {
//            System.err.println("url:\n"+format(url + "/{id}", id));
//            System.err.println("参数:");
//            asserts(
//                    mockMvc.perform(
//                            MockMvcRequestBuilders.delete(url + "/{id}", id)
//                    )
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return this;
//    }
//
//    public Tester markDeleteById(String id) {
//        try {
//            System.err.println("url:\n"+format(url + "/{id}", id));
//            System.err.println("参数:");
//            asserts(
//                    mockMvc.perform(
//                            MockMvcRequestBuilders.patch(url + "/{id}", id)
//                    )
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return this;
//    }
//
//    public Tester markDeleteByIds(String... ids) {
//        try {
//            System.err.println("url:\n"+url);
//            System.err.println("参数:\n"+new ParamsInfo(ids).toString());
//            asserts(
//                    mockMvc.perform(
//                            MockMvcRequestBuilders
//                                    .patch(url)
//                                    .content(new ParamsInfo(ids).toString())
//                    )
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return this;
//    }
//
//    public Tester getById(String id) {
//        try {
//            System.err.println("url:\n"+format(url + "/{id}", id));
//            System.err.println("参数:");
//            asserts(
//                    mockMvc.perform(
//                            MockMvcRequestBuilders.get(url + "/{id}", id)
//                    )
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return this;
//    }
//
//    public Tester search(int pageIndex, int pageSize, Object params) {
//        try {
//            System.err.println("url:\n" + format(url + "/{pageIndex}/{pageSize}", pageIndex, pageSize));
//            System.err.println("参数:\n?params=" + URLEncoder.encode(JSON.toJSONString(params), "UTF-8"));
//            asserts(
//                    mockMvc.perform(
//                            MockMvcRequestBuilders
//                                    .get(url + "/{pageIndex}/{pageSize}", pageIndex, pageSize)
//                                    .param("params", JSON.toJSONString(params))
//                    )
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return this;
//    }
//
//}
