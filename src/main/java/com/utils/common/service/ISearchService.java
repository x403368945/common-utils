package com.utils.common.service;

import com.utils.common.entity.base.Sorts;
import com.utils.util.Pager;
import lombok.NonNull;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/**
 * 服务接口基础方法定义
 * 
 * @author Jason Xie 2017年7月14日 上午11:23:18
 * @param <E>
 */
public interface ISearchService<E, ID> {
//	IRepository<E> getFindRepository();

	/**
	 * 按ID查询对象
	 * @param id ID 数据ID
	 * @return Optional<E> 实体对象
	 * @throws Exception 查询异常
	 */
	default Optional<E> getById(@NonNull final ID id) throws Exception {
		throw new NullPointerException(this.getClass().getName() + "：方法【getById(@NonNull final ID id)】未实现");
	}
	/**
	 * 按条件查询列表
	 * @param condition 查询条件
	 * @param sorts 排序集合
	 * @return List<E> 结果集合
	 * @throws Exception 查询异常
	 */
	default List<E> findList(@NonNull final E condition, Sorts... sorts) throws Exception {
//		return getFindRepository().findList(condition, sorts);
		throw new NullPointerException(this.getClass().getName() + "：方法【findList(@NonNull final E condition, Sorts... sorts)】未实现");
	}

	/**
	 * 按条件分页查询列表
	 * @param condition 查询条件
	 * @param pager Pager 分页排序集合
	 * @return Pager<E> 分页对象
	 * @throws Exception 查询异常
	 */
	default Page<E> findPage(@NonNull final E condition, Pager pager) throws Exception {
//		return getFindRepository().findPage(condition, pager);
		throw new NullPointerException(this.getClass().getName() + "：方法【findPage(@NonNull final E condition, Pager pager)】未实现");
	}
}