package com.utils.common.service;

import lombok.NonNull;

import java.util.List;

/**
 * 服务接口基础方法定义
 * 
 * @author Jason Xie 2017年7月14日 上午11:23:18
 * @param <E>
 */
public interface IService<E> extends ISearchService<E> {
//	IRepository<E> getFindRepository();
	/**
	 * 保存
	 * @param obj E 实体对象
	 * @param userId String 操作用户ID
	 * @return E 实体对象
	 * @throws Exception 保存异常
	 */
	default E save(@NonNull final E obj, final String userId) throws Exception {
//		Asserts.notEmpty(userId,"参数【userId】是必须的");
//		Params.notEmpty(obj.getName(), "字段【name】不能为空");
		throw new NullPointerException(this.getClass().getName() + "：方法【save(@NonNull final E obj)】未实现");
	}
	
	/**
	 * 批量保存
	 * @param list 实体对象集合
	 * @param userId String 操作用户ID
	 * @return List<E> 实体对象集合
	 * @throws Exception 保存异常
	 */
	default List<E> save(@NonNull final List<E> list, final String userId) throws Exception {
//		Asserts.notEmpty(userId,"参数【userId】是必须的");
//		Params.notEmpty(obj.getName(), "字段【name】不能为空");
		throw new NullPointerException(this.getClass().getName() + "：方法【save(@NonNull final List<E> list)】未实现");
	}
	
	/**
	 * 修改数据
	 * @param id String 数据ID
	 * @param userId String 操作用户ID
	 * @param obj E 实体对象
	 * @throws Exception 保存异常
	 */
	default void update(final String id, final String userId, @NonNull final E obj) throws Exception {
//		Asserts.notEmpty(id,"参数【id】是必须的");
//		Asserts.notEmpty(userId,"参数【userId】是必须的");
//		Params.notEmpty(obj.getName(), "字段【name】不能为空");
		throw new NullPointerException(this.getClass().getName() + "：方法【update(@NonNull final String id, @NonNull final String userId, @NonNull final E obj)】未实现");
	}
	
	/**
	 * 按ID删除，物理删除；执行物理删除前先查询到数据，等待删除成功之后返回该数据对象，通过 aop 拦截记录到删除日志中
	 * @param id String 数据ID
	 * @param userId String 操作用户ID
	 * @return E 删除对象数据实体
	 * @throws Exception 删除失败异常
	 */
	default E deleteById(final String id, final String userId) throws Exception {
//		Asserts.notEmpty(id,"参数【id】是必须的");
//		Asserts.notEmpty(userId,"参数【userId】是必须的");
		throw new NullPointerException(this.getClass().getName() + "：方法【deleteById(@NonNull final String id, @NonNull final String userId)】未实现");
	}
	
	/**
	 * 按ID删除，标记删除
	 * @param id String 数据ID
	 * @param userId String 操作用户ID
	 * @throws Exception 删除失败异常
	 */
	default void markDeleteById(final String id, final String userId) throws Exception {
//		Asserts.notEmpty(id,"参数【id】是必须的");
//		Asserts.notEmpty(userId,"参数【userId】是必须的");
		throw new NullPointerException(this.getClass().getName() + "：方法【markDeleteById(@NonNull final String id, @NonNull final String userId)】未实现");
	}
	
	/**
	 * 批量操作按ID删除，标记删除
	 * @param ids List<String> 数据ID
	 * @param userId String 操作用户ID
	 * @throws Exception 删除失败异常
	 */
	default void markDeleteById(final List<String> ids, final String userId) throws Exception {
//		Asserts.notEmpty(ids,"参数【ids】是必须的");
//		Asserts.notEmpty(userId,"参数【userId】是必须的");
		throw new NullPointerException(this.getClass().getName() + "：方法【markDeleteById(@NonNull final List<String> ids, @NonNull final String userId)】未实现");
	}
}