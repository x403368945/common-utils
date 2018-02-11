package com.utils.common.entity.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.querydsl.core.QueryResults;
import com.utils.enums.Code;
import com.utils.exception.CodeException;
import com.utils.exception.ParamsException;
import com.utils.exception.UserSessionException;
import com.utils.util.FWrite;
import com.utils.util.Maps;
import com.utils.util.Util;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import java.io.File;
import java.util.*;

/**
 * 返回结果集对象
 *
 * @author Jason Xie 2017-9-20
 */
@Slf4j
@JSONType(orders = {"code", "message", "rowCount", "pageCount", "totalCount", "exception", "data", "extras"})
public class ResultInfo<E> {

    /**
     * 默认构造函数
     */
    public ResultInfo() {
        this(Code.FAILURE);
    }

    /**
     * 带参构造函数
     *
     * @param code Code 操作返回码
     */
    public ResultInfo(final Code code) {
        super();
        setCode(code);
    }

    /**
     * 编码，成功、失败、异常编码
     */
    private Code code;
    /**
     * 异常消息
     */
    private String exception;
    /**
     * 返回数据集合
     */
    private List<E> data = Collections.emptyList();
    /**
     * 总页数
     */
    private int pageCount;
    /**
     * 总行数
     */
    private long totalCount;
    /**
     * 本次响应数据总行数
     */
    private long rowCount;
    /**
     * 附加信息
     */
    private Map<String, Object> extras = null;

    public Code getCode() {
        return code;
    }

    public ResultInfo<E> setCode(final Code code) {
        this.code = (Objects.isNull(code) ? Code.FAILURE : code);
        return this; // 保证链式请求，返回:this
    }

//    @JSONField(serialize = false) // TODO 环境发布：生产环境不返回Exception，解开注释
    public String getException() {
        return exception;
    }

    public ResultInfo<E> setException(final String exception) {
        this.exception = exception;
        return this; // 保证链式请求，返回:this
    }

    public List<E> getData() {
        return data;
    }

    /**
     * JSON 字符串反序列化时需要此方法，其他情况禁止直接调用 setData 方法，请使用 setSuccess 方法
     *
     * @param data List<E>
     * @deprecated 禁止直接调用 setData 方法，请使用 setSuccess 方法
     */
    @Deprecated
    public void setData(final List<E> data) {
        this.data = data;
    }

    public int getPageCount() {
        return pageCount;
    }

    public ResultInfo<E> setPageCount(int pageCount) {
        this.pageCount = pageCount;
        return this; // 保证链式请求，返回:this
    }

    public long getTotalCount() {
        return totalCount;
    }

    public ResultInfo<E> setTotalCount(final long totalCount) {
        this.totalCount = totalCount;
        return this; // 保证链式请求，返回:this
    }

    public long getRowCount() {
        return rowCount;
    }

    public ResultInfo<E> setRowCount(final long rowCount) {
        this.rowCount = rowCount;
        return this; // 保证链式请求，返回:this
    }

    public Map<String, Object> getExtras() {
        return extras;
    }

    public ResultInfo<E> setExtras(final Map<String, Object> extras) {
        this.extras = extras;
        return this; // 保证链式请求，返回:this
    }

    // 扩展方法：start *************************************************************************************************************************************************
    /**
     * 将编码转换成具体消息
     *
     * @return String
     */
    public String getMessage() {
        return this.code.comment;
    }

    /**
     * 设置异常编码及异常信息
     *
     * @param code      Code 异常编码
     * @param exception String 异常消息内容
     */
    public ResultInfo<E> setCode(final Code code, final String exception) {
        setCode(code);
        this.exception = exception;
        return this; // 保证链式请求，返回:this
    }

    /**
     * 将业务逻辑中捕获到的异常转换为对应的code
     *
     * @param e Exception 捕获到的异常
     * @return ResultInfo<E>
     */
    public ResultInfo<E> setCode(final Exception e) {
        if (e instanceof CodeException) {
            setCode(((CodeException) e).getCode(), e.getMessage());
        } else if (e instanceof ParamsException) {
            setCode(Code.PARAMS_ERROR, e.getMessage());
        } else if (e instanceof UserSessionException) {
            setCode(Code.SESSION_TIMEOUT, e.getMessage());
        } else {
            setCode(Code.FAILURE, e.getMessage());
        }
        return this; // 保证链式请求，返回:this
    }

    /**
     * 重载方法，设置成功后的数据集合；返回当前对象，便于链式调用
     *
     * @param page Pager<E>
     * @return ResultInfo<E>
     */
    public ResultInfo<E> setSuccess(final Page<E> page) {
        this.code = Code.SUCCESS;
        if(Util.isNotEmpty(page)) {
            this.totalCount = page.getTotalElements();
            this.pageCount = page.getTotalPages();
            setSuccess(page.getContent());
        }
        return this; // 保证链式请求，返回:this
    }
    /**
     * 重载方法，设置成功后的数据集合；返回当前对象，便于链式调用
     *
     * @param page Pager<E>
     * @return ResultInfo<E>
     */
    public ResultInfo<E> setSuccess(final QueryResults<E> page) {
        this.code = Code.SUCCESS;
        if(Util.isNotEmpty(page)) {
            this.totalCount = page.getTotal();
            this.pageCount = Util.getPageCount((int)page.getLimit(), (int)page.getTotal());
            setSuccess(page.getResults());
        }
        return this; // 保证链式请求，返回:this
    }

    /**
     * 重载方法，设置成功后的数据集合；返回当前对象，便于链式调用
     *
     * @param data List<E>
     * @return ResultInfo<E>
     */
    public ResultInfo<E> setSuccess(final List<E> data) {
        this.code = Code.SUCCESS;
        this.data = Util.isNotEmpty(data) ? data : Collections.emptyList(); // 设置有效的结果集
        this.rowCount = this.data.size(); // 设置结果集大小
        return this; // 保证链式请求，返回:this
    }
    /**
     * 重载方法，设置成功后的数据集合；返回当前对象，便于链式调用
     *
     * @param data E[]
     * @return ResultInfo<E>
     */
    public ResultInfo<E> setSuccess(final E... data) {
        this.code = Code.SUCCESS;
        this.data = (data.length > 1 || (data.length == 1 && Util.isNotEmpty(data[0]))) ? Arrays.asList(data) : Collections.emptyList(); // 设置有效的结果集
        this.rowCount = this.data.size(); // 设置结果集大小
        return this; // 保证链式请求，返回:this
    }

    /**
     * 添加扩展属性，返回ResultInfo对象本身，支持链式请求
     *
     * @param key   String
     * @param value Object
     * @return ResultInfo<E>
     */
    public ResultInfo<E> addExtras(@NonNull final String key, final Object value) {
        if (Util.isEmpty(this.extras)) this.extras = new HashMap<>();
        this.extras.put(key, value);
        return this; // 保证链式请求，返回:this
    }
    /**
     * 添加扩展属性，返回ResultInfo对象本身，支持链式请求
     *
     * @return ResultInfo<E>
     */
    public ResultInfo<E> addExtras(@NonNull final JSONObject obj) {
        if (Util.isEmpty(this.extras)) this.extras = new HashMap<>();
        this.extras.putAll(obj);
        return this; // 保证链式请求，返回:this
    }

    /**
     * 判断 code 是否为 SUCCESS
     *
     * @return ResultInfo<E> code == SUCCESS 返回结果集对象
     * @throws CodeException code != SUCCESS 则抛出异常
     */
    @JSONField(serialize = false, deserialize = false)
    public ResultInfo<E> isSuccess() throws CodeException {
        if (Code.SUCCESS == this.code) return this; // return this; 保证链式请求
        throw this.code.exception(this.exception);
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
     * 获取data集合中的第一项;获取前先校验 isRowCount()
     * @return E
     */
    public Optional<E> dataFirst() {
        return (this.rowCount == 0)
                ? Optional.empty()
                : Optional.of(this.data.get(0));
    }

    /**
     * 将对象写入文件，返回文件路径
     * @param file File 写入文件
     * @return Optional<String>
     */
    public Optional<String> write(@NonNull final File file) {
        return FWrite.of(file).write(this.toString()).getFilePath();
    }

    /**
     * 将json格式转换为ResultInfo对象
     *
     * @param jsonText Json文本
     * @return ResultInfo<X>
     */
    public static ResultInfo<Object> ofJson(@NonNull final String jsonText) {
        return JSON.parseObject(jsonText, new TypeReference<ResultInfo<Object>>(){});
    }
    public static ResultInfo<Object> of(final Object obj) {
        return new ResultInfo<>().setSuccess(obj);
    }
    public static ResultInfo<String> ofString(final String text) {
        return new ResultInfo<String>().setSuccess(text);
    }


    // 扩展方法：end *************************************************************************************************************************************************

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static void main(String[] args) {
        {
            log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 打印所有状态码 <<<<<<<<<<<<<<<<<<");
            for (Code code : Code.values()) {
                log.debug(code + ":" + code.comment);
            }
        }
        {
            log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 打印对象 toJson 之后的全部字段 <<<<<<<<<<<<<<<<<<");
            log.debug(JSON.toJSONString(new ResultInfo<>(),
                    SerializerFeature.WriteMapNullValue,
                    SerializerFeature.WriteNullBooleanAsFalse,
                    SerializerFeature.WriteNullListAsEmpty,
                    SerializerFeature.WriteNullNumberAsZero,
                    SerializerFeature.WriteNullStringAsEmpty
            ));
        }
        {
            log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 声明data集合中只能是String <<<<<<<<<<<<<<<<<<");
            ResultInfo<String> resultInfo = new ResultInfo<>();
            // 设置单一对象，必须是泛型声明的类型
            resultInfo.setSuccess("111");
            log.debug(resultInfo.toString());
            // 设置多个对象，必须是泛型声明的类型
            resultInfo.setSuccess("222", "333");
            log.debug(resultInfo.toString());
            // 设置对象对象数组，必须是泛型声明的类型
            resultInfo.setSuccess(new String[]{"444", "555"});
            log.debug(resultInfo.toString());
            // 设置对象集合，必须是泛型声明的类型
            resultInfo.setSuccess(Arrays.asList("666", "777"));
            // 带有附加属性(扩展属性),可以链式调用
            resultInfo.addExtras("name","Jason").addExtras("amount",100).addExtras("roles", new String[]{"ROLE_USER"});
            log.debug(resultInfo.toString());
        }
        {
            log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 声明data集合中只能是Map<String, Object> <<<<<<<<<<<<<<<<<<");
            ResultInfo<Map<String, Object>> resultInfo = new ResultInfo<>();
            // 设置单一对象，必须是泛型声明的类型
            resultInfo.setSuccess(Maps.bySO("key", "111"));
            log.debug(resultInfo.toString());
            // 设置多个对象，必须是泛型声明的类型
            resultInfo.setSuccess(Maps.bySO("key", "222"), Maps.bySO("key", "333"));
            log.debug(resultInfo.toString());
            // 设置对象集合，必须是泛型声明的类型
            resultInfo.setSuccess(Arrays.asList(Maps.bySO("key", "444"), Maps.bySO("key", "555")));
            // 带有附加属性(扩展属性),可以链式调用
            resultInfo.addExtras("name","Jason").addExtras("amount",100).addExtras("roles", new String[]{"ROLE_USER"});
            log.debug(resultInfo.toString());
        }
        {
            log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 声明data集合中只能是 ItemInfo <<<<<<<<<<<<<<<<<<");
            ResultInfo<ItemInfo> resultInfo = new ResultInfo<>();
            // 设置单一对象，必须是泛型声明的类型
            resultInfo.setSuccess(ItemInfo.builder().label("key").value(111).build());
            log.debug(resultInfo.toString());
            // 设置多个对象，必须是泛型声明的类型
            resultInfo.setSuccess(ItemInfo.builder().label("key").value(222).build(), ItemInfo.builder().label("key").value(333).build());
            log.debug(resultInfo.toString());
            // 设置对象对象数组，必须是泛型声明的类型
            resultInfo.setSuccess(new ItemInfo[]{ItemInfo.builder().label("key").value(444).build(), ItemInfo.builder().label("key").value(555).build()});
            log.debug(resultInfo.toString());
            // 设置对象集合，必须是泛型声明的类型
            resultInfo.setSuccess(Arrays.asList(ItemInfo.builder().label("key").value(666).build(), ItemInfo.builder().label("key").value(777).build()));
            // 带有附加属性(扩展属性),可以链式调用
            resultInfo.addExtras("name","Jason").addExtras("amount",100).addExtras("roles", new String[]{"ROLE_USER"});
            log.debug(resultInfo.toString());
        }
        {
            log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 将 JSON 字符串反序列化为ResultInfo对象 <<<<<<<<<<<<<<<<<<");
            log.debug(ResultInfo.ofJson("{\"code\":\"SUCCESS\",\"message\":\"成功\",\"rowCount\":90,\"pageCount\":100,\"totalCount\":200,\"data\":[\"A\",\"B\"]}").toString());
            log.debug(ResultInfo.ofJson("{\"code\":\"SUCCESS\",\"message\":\"成功\",\"rowCount\":90,\"pageCount\":100,\"totalCount\":200,\"data\":[\"A\",\"B\"]}").toString());
            log.debug(ResultInfo.ofJson("{\"code\":\"FAILURE\",\"message\":\"失败\",\"data\":[{\"name\":\"A\"},{\"name\":\"B\"}]}").toString());
        }
//        {
//            try {
//                ResultInfo<Table> resultInfo = JSON.parseObject(FileUtil.read("D:\\project\\anavss\\files\\upload-model-data\\c9d6ad96-3eed-4d70-879b-bead504f0730\\2018\\BudgetMainIncome\\66f871de-5265-462d-8f55-1e34baa0e286.json"), new TypeReference<ResultInfo<Table>>(){});
//                log.debug("{}",resultInfo.dataFirst());
//                log.debug(resultInfo.toString());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

    }
}
