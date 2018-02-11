package com.utils.common.entity.base;

import com.utils.common.entity.IJson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 事件消息对象
 * @author Jason Xie on 2017/11/16.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Accessors(chain = true)
public class Message implements IJson {
 /**
     * 可选的事件属性
     */
    private String event;
    /**
     * 消息状态：true 成功， false 失败
     */
    private boolean success;   
    /**
     * 异常消息内容
     */
    private String exception;

    @Override
    public String toString() {
        return json();
    }
}
