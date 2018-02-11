package com.utils.common.actions;

import com.utils.common.entity.base.ResultInfo;

/**
 * 动作行为组件接口 : 解析（文件上传成功之后，解析模板数据文件）
 * @param <R> ResultInfo -> data 存储的元素
 * @param <P> 入参数据对象
 * @author Jason Xie on 2017/10/12.
 */
public interface IParse<R, P> {
    /**
     * 解析（文件上传成功之后，解析模板数据文件）
     * @param params P 参数
     * @param userId String 用户ID
     * @return ResultInfo<R>
     */
    ResultInfo<R> parse(P params, final String userId);
}