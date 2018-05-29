package com.utils.common.dao;

import com.utils.common.entity.base.Sorts;
import com.utils.util.Pager;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * 数据访问接口方法定义，默认继承 QDSL 查询，建议查询尽量使用 QDSL 语法
 * 
 * @author Jason Xie 2017年7月14日 上午11:23:18
 * @param <E>
 */
@NoRepositoryBean
public interface IRepository<E, ID> extends QuerydslPredicateExecutor<E> {
	/**
	 * 修改数据
	 * @param id ID 数据ID
	 * @param userId String 操作用户ID
	 * @param obj E 实体对象
	 * @return long 影响行数
	 * @throws Exception 更新异常
	 */
	default long update(@NonNull final ID id, @NonNull final String userId, @NonNull final E obj) throws Exception {
		throw new NullPointerException(this.getClass().getName() + "：方法【update(@NonNull final String id, @NonNull final String userId, @NonNull final E obj)】未实现");
	}

	/**
	 * 按ID删除，物理删除
	 * @param id ID 数据ID
	 * @param userId String 操作用户ID
	 * @return E 删除对象实体
	 * @throws Exception 删除失败异常
	 */
	default E deleteById(@NonNull final ID id, @NonNull final String userId) throws Exception {
		throw new NullPointerException(this.getClass().getName() + "：方法【deleteById(@NonNull final String userId, @NonNull final String id)】未实现");
	}

	/**
	 * 按ID删除，标记删除
	 * @param id ID 数据ID
	 * @param userId String 操作用户ID
	 * @return long 影响行数
	 * @throws Exception 删除失败异常
	 */
	default long markDeleteById(@NonNull final ID id, @NonNull final String userId) throws Exception {
		throw new NullPointerException(this.getClass().getName() + "：方法【markDeleteById(@NonNull final String id, @NonNull final String userId)】未实现");
	}

	/**
	 * 批量操作按ID删除，标记删除
	 * @param ids List<ID> 数据ID
	 * @param userId String 操作用户ID
	 * @return long 影响行数
	 * @throws Exception 删除失败异常
	 */
	default long markDeleteById(@NonNull final List<ID> ids, @NonNull final String userId) throws Exception {
		throw new NullPointerException(this.getClass().getName() + "：方法【markDeleteById(@NonNull final List<String> ids, @NonNull final String userId)】未实现");
	}

	/**
	 * 按条件查询列表
	 * @param condition 查询条件
	 * @param sorts 排序集合
	 * @return List<E> 结果集合
	 * @throws Exception 查询异常
	 */
	default List<E> findList(@NonNull final E condition, Sorts... sorts) throws Exception {
		throw new NullPointerException(this.getClass().getName() + "：方法【findList(@NonNull final E condition, Sorts sorts)】未实现");
	}

	/**
	 * 按条件分页查询列表
	 * @param condition 查询条件
	 * @param pager Pager 分页排序集合
	 * @return Page<E> 分页对象
	 * @throws Exception 查询异常
	 */
	default Page<E> findPage(@NonNull final E condition, Pager pager) throws Exception {
		throw new NullPointerException(this.getClass().getName() + "：方法【findPage(@NonNull final E condition, Pager pager)】未实现");
	}
}