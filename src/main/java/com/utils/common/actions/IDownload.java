package com.utils.common.actions;

import com.utils.common.entity.base.ResultInfo;

/**
 * 动作行为组件接口 : 下载（下载模型模板）
 * @param <R> ResultInfo -> data 存储的元素
 * @param <P> 入参数据对象
 * @author Jason Xie on 2017/10/12.
 */
public interface IDownload<R, P> {
    /**
     * 下载（下载模型模板）
     * @param params P 参数
     * @param userId String 用户ID
     * @return ResultInfo<R>
     */
    ResultInfo<R> download(P params, final String userId);
}