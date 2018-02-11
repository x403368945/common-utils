package com.utils.common.actions;

import com.utils.common.entity.base.ResultInfo;

/**
 * 动作行为组件接口 : 向外公开模型实现的功能及数据结构
 * @param <R> ResultInfo -> data 存储的元素
 * @author Jason Xie on 2017/10/12.
 */
public interface ISay<R> {
    /**
     * 向外公开模型实现的功能及数据结构
     * @param userId String 用户ID
     * @return ResultInfo<R>
     */
    ResultInfo<R> say(final String userId);
}