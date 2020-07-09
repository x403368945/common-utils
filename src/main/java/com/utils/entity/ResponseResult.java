package com.utils.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.utils.ICode;
import com.utils.IJson;
import com.utils.enums.Code;
import com.utils.exception.CodeException;
import com.utils.util.FWrite;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;

/**
 * 返回结果集对象
 *
 * @author 谢长春 2017-9-20
 */
@ToString
@Slf4j
@Accessors(chain = true)
@JSONType(orders = {"code", "message", "rowCount", "pageCount", "totalCount", "exception", "data", "extras"})
public class ResponseResult<E> implements IJson, Cloneable {

    /**
     * 默认构造函数
     */
    public ResponseResult() {
        this(Code.A00001);
    }

    /**
     * 带参构造函数
     *
     * @param code {@link ICode} 操作响应码
     */
    public ResponseResult(final ICode code) {
        super();
        setCode(code);
    }

    /**
     * 将json格式转换为ResponseResult对象
     *
     * @param jsonText {@link String} Json文本
     * @return ResponseResult<Object>
     */
    public static <T> ResponseResult<T> valueOfJson(final String jsonText) {
        Objects.requireNonNull(jsonText, "参数【jsonText】是必须的");
        return JSON.parseObject(jsonText, new TypeReference<ResponseResult<T>>() {
        });
    }

    /**
     * 将json格式转换为ResponseResult对象
     *
     * @param jsonText {@link String} Json文本
     * @return ResponseResult<Object>
     */
    public static <T> ResponseResult<T> valueOfJson(final String jsonText, final TypeReference<T> type) {
        Objects.requireNonNull(jsonText, "参数【jsonText】是必须的");
        return JSON.parseObject(jsonText, type.getType());
    }

    /**
     * 编码，成功、失败、异常编码
     */
    @Getter
    private ICode code;
    /**
     * 本次响应数据总行数
     */
    @Getter
    @Setter
    private long rowCount;
    /**
     * 总页数
     */
    @Getter
    @Setter
    private int pageCount;
    /**
     * 总行数
     */
    @Getter
    @Setter
    private long totalCount;
    /**
     * 异常消息
     */
    @Getter
    private String exception;
    /**
     * 返回数据集合
     */
    @Getter
    private List<E> data = Collections.emptyList();
    /**
     * 附加信息
     */
    @Getter
    @Setter
    private Map<String, Object> extras = null;

    /**
     * 将编码转换成具体消息
     *
     * @return String
     */
    public String getMessage() {
        if (Objects.equals(Code.A00002, code)) {
            // 处理自定义动态异常消息
            return Objects.isNull(this.exception)
                    ? null
                    : this.exception.replace(Code.A00002.name().concat(":"), "");
        }
        return this.code.getComment();
    }

//    /**
//     * 生产环境不返回接口版本信息
//     *
//     * @return {@link Version}
//     */
//    public Version getVersion() {
//        return AppConfig.isProd() ? null : version;
//    }

    public ResponseResult<E> setCode(final ICode code) {
        this.code = (Objects.isNull(code) ? Code.A00001 : code);
        return this; // 保证链式请求，返回:this
    }

    /**
     * JSON 字符串反序列化时需要此方法，其他情况禁止直接调用 setData 方法，请使用 {@link ResponseResult#setSuccess(List)} 方法
     *
     * @param data List<E>
     * @deprecated 禁止直接调用 setData 方法，请使用 {@link ResponseResult#setSuccess(List)} 方法
     */
    @Deprecated
    public void setData(final List<E> data) {
        this.data = data;
    }

    public ResponseResult<E> setException(final String exception) {
        this.exception = exception;
        return this; // 保证链式请求，返回:this
    }

    /**
     * 设置异常编码及异常信息
     *
     * @param code      {@link ICode} 异常响应码
     * @param exception String 异常消息内容
     */
    public ResponseResult<E> setException(final ICode code, final String exception) {
        setCode(Objects.equals(code, Code.A00000) ? Code.A00001 : code);
        this.exception = exception;
        return this; // 保证链式请求，返回:this
    }

    /**
     * 将业务逻辑中捕获到的异常转换为对应的code
     *
     * @param e {@link Exception} 捕获到的异常
     * @return ResponseResult<E>
     */
    public ResponseResult<E> setException(final Exception e) {
        if (e instanceof CodeException) {
            setException(((CodeException) e).getCode(), e.getMessage());
//        } else if (e instanceof ValidationException || e instanceof IllegalArgumentException) {
//            setException(Code.VALIDATED, e.getMessage());
        } else {
            setException(Code.A00001, e.getMessage());
        }
        return this; // 保证链式请求，返回:this
    }

    /**
     * 重载方法，设置成功后的数据集合；返回当前对象，便于链式调用
     *
     * @param data List<E>
     * @return ResponseResult<E>
     */
    public ResponseResult<E> setSuccess(final List<E> data) {
        this.code = Code.A00000;
        this.data = Objects.nonNull(data) ? data : Collections.emptyList(); // 设置有效的结果集
        this.rowCount = this.data.size(); // 设置结果集大小
        return this; // 保证链式请求，返回:this
    }

    /**
     * 重载方法，设置成功后的数据集合；返回当前对象，便于链式调用
     *
     * @param data E[]
     * @return ResponseResult<E>
     */
    public ResponseResult<E> setSuccess(final E[] data) {
        this.code = Code.A00000;
        this.data = (data.length > 0 && Objects.nonNull(data[0])) ? Arrays.asList(data) : Collections.emptyList(); // 设置有效的结果集
        this.rowCount = this.data.size(); // 设置结果集大小
        return this; // 保证链式请求，返回:this
    }

    /**
     * 重载方法，设置成功后的数据集合；返回当前对象，便于链式调用
     *
     * @param data E
     * @return ResponseResult<E>
     */
    public ResponseResult<E> setSuccess(final E data) {
        this.code = Code.A00000;
        if (Objects.nonNull(data)) {
            this.data = Collections.singletonList(data);
        } else {
            this.data = Collections.emptyList();
        }
        this.rowCount = this.data.size(); // 设置结果集大小
        return this; // 保证链式请求，返回:this
    }

    /**
     * 添加扩展属性，返回ResponseResult对象本身，支持链式请求
     *
     * @param key   String
     * @param value Object
     * @return ResponseResult<E>
     */
    public ResponseResult<E> addExtras(final String key, final Object value) {
        Objects.requireNonNull(key, "参数【key】是必须的");
        if (Objects.isNull(this.extras)) {
            this.extras = new HashMap<>(10);
        }
        this.extras.put(key, value);
        return this; // 保证链式请求，返回:this
    }

    /**
     * 添加扩展属性，返回ResponseResult对象本身，支持链式请求
     *
     * @return ResponseResult<E>
     */
    public ResponseResult<E> addExtras(final JSONObject obj) {
        Objects.requireNonNull(obj, "参数【obj】是必须的");
        if (Objects.isNull(this.extras)) {
            this.extras = new HashMap<>(10);
        }
        this.extras.putAll(obj);
        return this; // 保证链式请求，返回:this
    }

    /**
     * 添加扩展属性，返回ResponseResult对象本身，支持链式请求
     *
     * @return ResponseResult<E>
     */
    public ResponseResult<E> addExtras(final Map<String, String> extras) {
        if (Objects.isNull(this.extras)) {
            this.extras = new HashMap<>(10);
        }
        this.extras.putAll(extras);
        return this; // 保证链式请求，返回:this
    }

    /**
     * 判断 code 是否为 SUCCESS
     *
     * @return ResponseResult<E> code == SUCCESS 返回结果集对象
     * @throws CodeException code != SUCCESS 则抛出异常
     */
    @JSONField(serialize = false, deserialize = false)
    public ResponseResult<E> isSuccess() throws CodeException {
        if (Code.A00000 == this.code) {
            return this; // return this; 保证链式请求
        }
        throw new CodeException(this.code, this.exception);
    }

    /**
     * 判断 code 是否为 SUCCESS
     *
     * @return ResponseResult<E> code == SUCCESS 返回结果集对象
     * @throws CodeException code != SUCCESS 则抛出异常
     */
    @JSONField(serialize = false, deserialize = false)
    public ResponseResult<E> assertSuccess() throws CodeException {
        if (Code.A00000 == this.code) {
            return this; // return this; 保证链式请求
        }
        throw new CodeException(this.code, this.exception);
    }

    /**
     * 判断 rowCount 是否大于0
     *
     * @return true大于0，false等于0
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isRowCount() {
        return this.rowCount > 0;
    }

    /**
     * 获取data集合中的第一项;获取前先校验集合长度是否大于0
     *
     * @return E
     */
    public Optional<E> dataFirst() {
        return (this.rowCount == 0)
                ? Optional.empty()
                : Optional.of(this.data.get(0));
    }

    /**
     * 将对象写入文件，返回文件路径
     *
     * @param file    {@link File} 写入文件
     * @param feature {@link SerializerFeature}
     * @return Optional<FileWrite>
     */
    public FWrite writeJson(final File file, SerializerFeature... feature) {
        Objects.requireNonNull(file, "参数【file】是必须的");
        return FWrite.of(file).write(JSON.toJSONString(this, feature));
    }

//    /**
//     * 弱校验版本号；不会抛出异常，只是在 extras 中添加版本号不匹配；
//     *
//     * @param version int 用户请求版本号
//     * @return {@link ResponseResult}{@link ResponseResult<E>}
//     */
//    public ResponseResult<E> versionAssert(final int version) {
//        return versionAssert(version, false);
//    }
//
//    /**
//     * 强校验版本号；版本号不匹配将会抛出【{@link Code#VERSION}】异常
//     * 如果需要弱校验，可以使用 {@link ResponseResult#versionAssert(int, boolean)}
//     *
//     * @param version int 用户请求版本号
//     * @return {@link ResponseResult}{@link ResponseResult<E>}
//     */
//    public ResponseResult<E> versionAssertStrict(final int version) {
//        return versionAssert(version, true);
//    }
//
//    /**
//     * 指定 exception 为 false 弱校验版本号；不会抛出异常，只是在 extras 中添加版本号不匹配；
//     *
//     * @param version   int 用户请求版本号
//     * @param exception boolean true:抛出异常，false:extras中添加 version 校验异常消息
//     * @return {@link ResponseResult}{@link ResponseResult<E>}
//     */
//    private ResponseResult<E> versionAssert(final int version, final boolean exception) {
//        if (!Objects.equals(this.v, version)) {
//            if (exception) {
//                throw Code.VERSION.exception("当前请求版本号与接口最新版本号不匹配");
//            } else {
//                addExtras("version", "当前请求版本号与接口最新版本号不匹配，请检查更新内容");
//            }
//        }
//        return this;
//    }
//
//    /**
//     * 设置版本信息
//     *
//     * @return {@link ResponseResult}{@link ResponseResult<E>}
//     */
//    public ResponseResult<E> version(final Function<Version.VersionBuilder, Version> version) {
//        try {
//            Assert.isTrue(v > 0, "指定版本前先指定【v】字段，当前接口最新版本号，版本号最小值为1");
//            this.version = Objects.requireNonNull(version, "参数【version】不能为null").apply(Version.builder().id(v));
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//        return this;
//    }
//
//    /**
//     * 设置版本信息
//     *
//     * @return {@link ResponseResult}{@link ResponseResult<E>}
//     */
//    public ResponseResult<E> version(final Class<?> clazz, final Function<Version.VersionBuilder, Version> version) {
//        try {
//            Assert.isTrue(v > 0, "指定版本前先指定【v】字段，当前接口最新版本号，版本号最小值为1");
//            Objects.requireNonNull(version, "参数【version】不能为null");
//            final String method = Thread.currentThread().getStackTrace()[2].getMethodName();
//            final Version.VersionBuilder builder = Version.builder().id(v).markdown(String.format("%s/%s.md", clazz.getSimpleName(), method));
//            for (Method m : clazz.getMethods()) {
////                System.out.println(m.getName());
//                if (Objects.equals(method, m.getName())) {
//                    for (Annotation annotation : m.getAnnotations()) {
//                        if (annotation instanceof PostMapping) {
//                            builder.method(POST).url(((RequestMapping) clazz.getAnnotation(RequestMapping.class)).value()[0] // 获取类头部 @RequestMapping 注解
//                                    .concat(Optional.of(((PostMapping) annotation).value()).map(arr -> arr.length > 0 ? arr[0] : "").orElse("")));
//                        } else if (annotation instanceof PutMapping) {
//                            builder.method(PUT).url(((RequestMapping) clazz.getAnnotation(RequestMapping.class)).value()[0] // 获取类头部 @RequestMapping 注解
//                                    .concat(Optional.of(((PutMapping) annotation).value()).map(arr -> arr.length > 0 ? arr[0] : "").orElse("")));
//                        } else if (annotation instanceof PatchMapping) {
//                            builder.method(PATCH).url(((RequestMapping) clazz.getAnnotation(RequestMapping.class)).value()[0] // 获取类头部 @RequestMapping 注解
//                                    .concat(Optional.of(((PatchMapping) annotation).value()).map(arr -> arr.length > 0 ? arr[0] : "").orElse("")));
//                        } else if (annotation instanceof DeleteMapping) {
//                            builder.method(DELETE).url(((RequestMapping) clazz.getAnnotation(RequestMapping.class)).value()[0] // 获取类头部 @RequestMapping 注解
//                                    .concat(Optional.of(((DeleteMapping) annotation).value()).map(arr -> arr.length > 0 ? arr[0] : "").orElse("")));
//                        } else if (annotation instanceof GetMapping) {
//                            builder.method(GET).url(((RequestMapping) clazz.getAnnotation(RequestMapping.class)).value()[0] // 获取类头部 @RequestMapping 注解
//                                    .concat(Optional.of(((GetMapping) annotation).value()).map(arr -> arr.length > 0 ? arr[0] : "").orElse("")));
//                        } else if (annotation instanceof RequestMapping) {
//                            builder.method(((RequestMapping) annotation).method()[0]).url(((RequestMapping) clazz.getAnnotation(RequestMapping.class)).value()[0]
//                                    .concat(Optional.of(((RequestMapping) annotation).value()).map(arr -> arr.length > 0 ? arr[0] : "").orElse("")));
//                        }
//                    }
//                    break;
//                }
//            }
//            this.version = version.apply(builder);
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//        return this;
//    }
//
//    /**
//     * <pre>
//     * 执行 consumer 代码逻辑；
//     * 处理 consumer 异常，并设置到响应对象中，但不会调用 {@link ResponseResult#setCode}({@link Code#A00000})，需要在 consumer 中设置成功状态
//     * {@link ResponseResult#execute} 可执行多次，上一个 {@link ResponseResult#execute} 执行失败，并不会影响下一次 {@link ResponseResult#execute} 执行，响应状态以最后一次 {@link ResponseResult#execute} 指定的状态
//     *
//     * @param consumer {@link Consumer<ResponseResult>}
//     * @return {@link ResponseResult}{@link ResponseResult<E>}
//     */
//    public ResponseResult<E> execute(final Consumer<ResponseResult<E>> consumer) {
//        try {
//            consumer.accept(this);
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            setException(e);
//        }
//        return this;
//    }
//
//    /**
//     * 执行 call 代码逻辑；如果 call 不抛异常，则调用 {@link ResponseResult#setCode}({@link Code#A00000})
//     *
//     * @param call {@link ICall}
//     * @return {@link ResponseResult}{@link ResponseResult<E>}
//     */
//    public ResponseResult<E> call(final ICall call) {
//        try {
//            call.call();
//            setCode(Code.A00000);
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            setException(e);
//        }
//        return this;
//    }
//
//    /**
//     * <pre>
//     * 同 {@link ResponseResult#execute} 类似，但是会先判断 code == Code.A00000，才会执行 consumer
//     * 一般 then 应该在 {@link ResponseResult#execute} 之后，then 表示上一步执行状态为 SUCCESS , 才会继续执行
//     *
//     * @param consumer {@link Consumer<ResponseResult>}
//     * @return {@link ResponseResult}{@link ResponseResult<E>}
//     */
//    public ResponseResult<E> then(final Consumer<ResponseResult<E>> consumer) {
//        if (Code.A00000 == code) {
//            try {
//                consumer.accept(this);
//            } catch (Exception e) {
//                log.error(e.getMessage(), e);
//                setException(e);
//            }
//        }
//        return this;
//    }
    // 扩展方法：end *************************************************************************************************************************************************

    /**
     * 用于打印日志时，清除 data 集合
     */
    public void clearData() {
        this.data = Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    @Override
    public ResponseResult<E> clone() {
        return (ResponseResult<E>) super.clone();
    }

    //    public static void main(String[] args) {
//        {
//            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 打印所有状态码 <<<<<<<<<<<<<<<<<<");
//            for (Code code : Code.values()) {
//                log.info(code + ":" + code.comment);
//            }
//        }
//        {
//            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 打印对象 toJson 之后的全部字段 <<<<<<<<<<<<<<<<<<");
//            log.info(JSON.toJSONString(new ResponseResult<>(),
//                    SerializerFeature.WriteMapNullValue,
//                    SerializerFeature.WriteNullBooleanAsFalse,
//                    SerializerFeature.WriteNullListAsEmpty,
//                    SerializerFeature.WriteNullNumberAsZero,
//                    SerializerFeature.WriteNullStringAsEmpty
//            ));
//        }
//        {
//            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 声明data集合中只能是String <<<<<<<<<<<<<<<<<<");
//            ResponseResult<String> result = new ResponseResult<>();
//            // 设置单一对象，必须是泛型声明的类型
//            result.setSuccess("111");
//            log.info(result.json());
//            // 设置多个对象，必须是泛型声明的类型
//            result.setSuccess(Arrays.asList("222", "333"));
//            log.info(result.json());
//            // 设置对象对象数组，必须是泛型声明的类型
//            result.setSuccess(new String[]{"444", "555"});
//            log.info(result.json());
//            // 设置对象集合，必须是泛型声明的类型
//            result.setSuccess(Arrays.asList("666", "777"));
//            // 带有附加属性(扩展属性),可以链式调用
//            result.addExtras("name", "JX").addExtras("amount", 100).addExtras("roles", new String[]{"ROLE_USER"});
//            log.info(result.json());
//        }
//        {
//            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 声明data集合中只能是Map<String, Object> <<<<<<<<<<<<<<<<<<");
//            ResponseResult<Map<String, Object>> result = new ResponseResult<>();
//            // 设置单一对象，必须是泛型声明的类型
//            result.setSuccess(Maps.bySO("key", "111"));
//            log.info(result.json());
//            // 设置多个对象，必须是泛型声明的类型
//            result.setSuccess(Arrays.asList(Maps.bySO("key", "222"), Maps.bySO("key", "333")));
//            log.info(result.json());
//            // 设置对象集合，必须是泛型声明的类型
//            result.setSuccess(Arrays.asList(Maps.bySO("key", "444"), Maps.bySO("key", "555")));
//            // 带有附加属性(扩展属性),可以链式调用
//            result.addExtras("name", "JX").addExtras("amount", 100).addExtras("roles", new String[]{"ROLE_USER"});
//            log.info(result.json());
//        }
//        {
//            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 声明data集合中只能是 Item <<<<<<<<<<<<<<<<<<");
//            ResponseResult<Item> result = new ResponseResult<>();
//            // 设置单一对象，必须是泛型声明的类型
//            result.setSuccess(Item.builder().key("key").value(111).build());
//            log.info(result.json());
//            // 设置多个对象，必须是泛型声明的类型
//            result.setSuccess(Arrays.asList(Item.builder().key("key").value(222).build(), Item.builder().key("key").value(333).build()));
//            log.info(result.json());
//            // 设置对象对象数组，必须是泛型声明的类型
//            result.setSuccess(new Item[]{Item.builder().key("key").value(444).build(), Item.builder().key("key").value(555).build()});
//            log.info(result.json());
//            // 设置对象集合，必须是泛型声明的类型
//            result.setSuccess(Arrays.asList(Item.builder().key("key").value(666).build(), Item.builder().key("key").value(777).build()));
//            // 带有附加属性(扩展属性),可以链式调用
//            result.addExtras("name", "JX").addExtras("amount", 100).addExtras("roles", new String[]{"ROLE_USER"});
//            log.info(result.json());
//        }
//        {
//            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 将 JSON 字符串反序列化为ResponseResult对象 <<<<<<<<<<<<<<<<<<");
//            log.info(ResponseResult.valueOfJson("{\"code\":\"SUCCESS\",\"message\":\"成功\",\"rowCount\":90,\"pageCount\":100,\"totalCount\":200,\"data\":[\"A\",\"B\"]}").json());
//            log.info(ResponseResult.valueOfJson("{\"code\":\"SUCCESS\",\"message\":\"成功\",\"rowCount\":90,\"pageCount\":100,\"totalCount\":200,\"data\":[\"A\",\"B\"]}").json());
//            log.info(ResponseResult.valueOfJson("{\"code\":\"FAILURE\",\"message\":\"失败\",\"data\":[{\"name\":\"A\"},{\"name\":\"B\"}]}").json());
//        }
////        {
////            try {
////                ResponseResult<Table> result = JSON.parseObject(FileUtil.read("D:\\project\\files\\upload-com-data\\c9d6ad96-3eed-4d70-879b-bead504f0730\\2018\\BudgetMainIncome\\66f871de-5265-462d-8f55-1e34baa0e286.json"), new TypeReference<ResponseResult<Table>>(){});
////                log.info("{}",result.dataFirst());
////                log.info(result.json());
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////        }
//
//    }
    public static void main(String[] args) {

        System.out.println(new ResponseResult<>().json());
        System.out.println(new ResponseResult<>(Code.A00004).json());
        System.out.println(new ResponseResult<>(Code.A00002).setException("异常消息").json());
        System.out.println(new ResponseResult<>(Code.A00001).setException("异常消息").json());
//        System.out.println(new ResponseResult<>(AppCode.APP_CODE_ERROR).json());
    }
}
