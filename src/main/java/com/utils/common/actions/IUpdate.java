package com.utils.common.actions;

import com.utils.common.entity.base.ResultInfo;

/**
 * 动作行为组件接口 : 更新数据（在线编辑后的数据需要刷新公式计算的结果）
 * @param <R> ResultInfo -> data 存储的元素
 * @param <P> 入参数据对象
 * @author Jason Xie on 2017/10/12.
 */
public interface IUpdate<R, P> {
    /**
     * 保存（保存数据数据）
     * @param params P 参数
     * @param userId String 用户ID
     * @return ResultInfo<R>
     */
    ResultInfo<R> update(P params, final String userId);
}