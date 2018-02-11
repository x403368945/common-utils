package com.utils.common.actions;

import com.utils.common.entity.base.ResultInfo;

/**
 * 动作行为组件接口 : 上传（上传模型模板数据）
 * @param <R> ResultInfo -> data 存储的元素
 * @param <P> 入参数据对象
 * @author Jason Xie on 2017/10/12.
 */
public interface IUpload<R, P> {
    /**
     * 上传（上传模型模板数据）
     * @param params P 参数
     * @param userId String 用户ID
     * @return ResultInfo<R>
     */
    ResultInfo<R> upload(P params, final String userId);
}