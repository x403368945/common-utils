package com.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.utils.util.FWrite;
import com.utils.util.Op;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;


/**
 * 读写 JSON 文件
 *
 * @author 谢长春 2017-9-26 .
 */
public interface IJsonFile {
    /**
     * @return {@link File} 读写文件
     */
    @JSONField(serialize = false)
    File getFile();

    /**
     * 将对象写入文件，返回写入对象；默认序列化 this 对象
     *
     * @param feature {@link SerializerFeature}
     * @return Optional<FWrite>
     */
    default FWrite writeJson(SerializerFeature... feature) {
        return FWrite
                .of(Objects.requireNonNull(getFile(), "必须实现【com.utils.IJsonFile#getFile()】方法"))
                .write(JSON.toJSONString(this, feature));
    }

    /**
     * 将对象写入文件，返回写入对象
     *
     * @param obj     {@link Object} 序列化对象
     * @param feature {@link SerializerFeature}
     * @return Optional<FWrite>
     */
    default FWrite writeJson(final Object obj, SerializerFeature... feature) {
        return FWrite
                .of(Objects.requireNonNull(getFile(), "必须实现【com.utils.IJsonFile#getFile()】方法"))
                .write(JSON.toJSONString(obj, feature));
    }

    /**
     * 读取 json 文件内容，读取前检查文件是否存在
     *
     * @return {@link Optional<File>}
     */
    @SneakyThrows
    default Optional<String> readJson() {
        return Optional
                .of(Objects.requireNonNull(getFile(), "必须实现【com.utils.IJsonFile#getFile()】方法"))
                .filter(File::exists)
                .map(file -> new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
    }

    /**
     * 检查文件是否存在
     *
     * @return {@link Op <File>}
     */
    default Optional<File> jsonExist() {
        return Optional
                .of(Objects.requireNonNull(getFile(), "必须实现【com.utils.IJsonFile#getFile()】方法"))
                .filter(File::exists);
    }
}