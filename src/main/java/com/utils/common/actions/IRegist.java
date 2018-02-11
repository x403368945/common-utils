package com.utils.common.actions;

import com.utils.common.entity.base.Message;

/**
 * 动作行为组件接口：模型注册或更新
 * @author Jason Xie on 2017/11/14.
         */
public interface IRegist {
    /**
     * 注册模型，返回注册状态消息
     * @return Message
     */
    Message regist();
}