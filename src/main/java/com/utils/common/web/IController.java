package com.utils.common.web;

import com.utils.common.entity.base.ParamsInfo;
import com.utils.common.entity.base.ResultInfo;
import com.utils.enums.Code;

/**
 * Controller 基础方法规范接口
 * 注释中有相应的代码实现模板，包括接参规范
 * 每个方法内部代码必须使用try{}catch(){},禁止Controller 方法抛出异常
 * 
 * @author Jason Xie 2017年7月14日 上午11:23:18
 * @param <E>
 */
public interface IController<E, ID> {
	/**
	 * 保存
	 * URL:/{模块url前缀}
	 * 参数：params=JSONObject
	 * @param paramsInfo ParamsInfo 参数对象
	 * @return ResultInfo<E>
	 */
	default ResultInfo<E> save(final ParamsInfo paramsInfo) {
		{
		    // 方法头注解
/*
            @PostMapping
            @ResponseBody
*/
            // 方法接参模板,@RequestBody(required = false)，设置为false可以让请求先过来，如果参数为空再抛出异常，保证本次请求能得到响应
/*
            @RequestBody(required = false) ParamsInfo paramsInfo) {
*/
		    // 代码模板
/*
			ResultInfo<E> resultInfo = new ResultInfo<>();
			try {
				Params.notEmpty(paramsInfo, "参数集合为空");
				resultInfo.setSuccess(service.save(paramsInfo.parseObject(E.class), getUserId()));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				resultInfo.setCode(e);
			}
			return resultInfo;
*/
		}
		return Code.FAILURE.toResult(this.getClass().getName() + "：方法【save(final ParamsInfo paramsInfo)】未实现");
	}

	/**
	 * 修改数据
	 * URL:/{模块url前缀}/{id}
	 * 参数：params=JSONObject
	 * @param id ID 数据ID
	 * @param paramsInfo ParamsInfo 参数对象
	 * @return ResultInfo<E>
	 */
	default ResultInfo<E> update(final ID id, ParamsInfo paramsInfo) {
		{
            // 方法头注解
/*
            @PutMapping("/{id}")
            @ResponseBody
*/
            // 方法接参模板,@RequestBody(required = false)，设置为false可以让请求先过来，如果参数为空再抛出异常，保证本次请求能得到响应
/*
            @PathVariable final String id, @RequestBody(required = false) ParamsInfo paramsInfo) {
*/
            // 代码模板
/*
			ResultInfo<E> resultInfo = new ResultInfo<>();
			try {
				Params.notEmpty(paramsInfo, "参数集合为空");
				service.update(id, getUserId(), paramsInfo.parseObject(E.class));
				resultInfo.setCode(SUCCESS);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				resultInfo.setCode(e);
			}
			return resultInfo;
*/
		}
		return Code.FAILURE.toResult(this.getClass().getName() + "：方法【update(final String id, ParamsInfo paramsInfo)】未实现");
	}
	
	/**
	 * 按ID删除，物理删除
	 * URL:/{模块url前缀}/{id}
	 * 参数：{id}数据ID；
	 * @param id ID  数据ID
	 * @return ResultInfo<Object>
	 */
	default ResultInfo<Object> deleteById(final ID id) {
		{
            // 方法头注解
/*
            @DeleteMapping("/{id}")
            @ResponseBody
*/
            // 方法接参模板
/*
            @PathVariable final String id) {
*/
            // 代码模板
/*
			ResultInfo<Object> resultInfo = new ResultInfo<>();
			try {
			    service.deleteById(id, getUserId());
			    resultInfo.setCode(Code.SUCCESS);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				resultInfo.setCode(e);
			}
			return resultInfo;
			return super.deleteById(service, id);
*/
		}
		return Code.FAILURE.toResult(this.getClass().getName() + "：方法【deleteById(final String id)】未实现");
	}
	
	/**
	 * 按ID删除，标记删除
	 * URL:/{模块url前缀}/{id}
	 * 参数：{id}数据ID；
	 * @param id String 数据ID
	 * @return ResultInfo<Object>
	 */
	default ResultInfo<Object> markDeleteById(final ID id) {
		{
            // 方法头注解
/*
            @PatchMapping("/{id}")
            @ResponseBody
*/
            // 方法接参模板
/*
            @PathVariable final String id) {
*/
		    // 代码模板
/*
			ResultInfo<Object> resultInfo = new ResultInfo<>();
			try {
			    service.markDeleteById(id, getUserId());
			    resultInfo.setCode(Code.SUCCESS);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				resultInfo.setCode(e);
			}
			return resultInfo;
			return super.markDeleteById(service, id);
*/
		}
		return Code.FAILURE.toResult(this.getClass().getName() + "：方法【markDeleteById(final String id)】未实现");
	}

	/**
	 * 批量操作按ID删除，标记删除
	 * URL:/{模块url前缀}
	 * 参数：params=JSONObject
	 * @param paramsInfo ParamsInfo 参数对象
	 * @return ResultInfo<Object>
	 */
	default ResultInfo<Object> markDeleteByIds(final ParamsInfo paramsInfo) {
		{
            // 方法头注解
/*
            @PatchMapping
            @ResponseBody
*/
            // 方法接参模板，params 设置默认值为 "{}" 的目的在于，保证不会因为未传参数进不了方法
/*
            @RequestBody(required = false) ParamsInfo paramsInfo) {
*/
            // 代码模板
/*
			ResultInfo<Object> resultInfo = new ResultInfo<>();
			try {
                Params.notEmpty(paramsInfo, "参数集合为空");
                Params.isTrue(paramsInfo.isArray(), "params 必须为数组");
                service.markDeleteById(paramsInfo.parseArray(String.class), getUserId());
			    resultInfo.setCode(Code.SUCCESS);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				resultInfo.setCode(e);
			}
			return resultInfo;
			return super.markDeleteById(service, paramsInfo);
*/
		}
		return Code.FAILURE.toResult(this.getClass().getName() + "：方法【markDeleteByIds(final ParamsInfo paramsInfo)】未实现");
	}
	
	/**
	 * 按ID查询
	 * URL:/{模块url前缀}/{id}
	 * 参数：{id}数据ID；
	 * @param id 数据ID
	 * @return ResultInfo<E>
	 */
	default ResultInfo<E> getById(final ID id) {
		{
            // 方法头注解
/*
            @GetMapping("/{id}")
            @ResponseBody
*/
            // 方法接参模板
/*
            @PathVariable final String id) {
*/
		    // 代码模板
/*
			ResultInfo<E> resultInfo = new ResultInfo<>();
			try {
                resultInfo.setSuccess(service.getById(id).orElse(null));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				resultInfo.setCode(e);
			}
			return resultInfo;
*/
		}
		return Code.FAILURE.toResult(this.getClass().getName() + "：方法【getById(final String id)】未实现");
	}

    /**
     * 按条件查询列表，不分页
     * URL:/{模块url前缀}
     * 参数：params=JSONObject
     * @param paramsInfo ParamsInfo 参数对象
     * @return ResultInfo<E>
     */
    default ResultInfo<E> search(final ParamsInfo paramsInfo) {
        {
            // 方法头注解
/*
            @GetMapping
            @ResponseBody
*/
            // 方法接参模板，params 设置默认值为 "{}" 的目的在于，保证不会因为未传参数进不了方法
/*
            @RequestParam(name = "params", required = false, defaultValue = "{}") final ParamsInfo paramsInfo) {
*/
            // 代码模板
/*
			ResultInfo<E> resultInfo = new ResultInfo<>();
			try {
			     resultInfo.setSuccess(
                    service.findList(
                    		paramsInfo.required(false) // false 可以指定查询参数不是必须的，如果不指定false，则会校验参数是必须的，否则抛出异常
                                .parseObject(E.class),
                            E.Order.name.desc
                    )
                );
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				resultInfo.setCode(e);
			}
			return resultInfo;
*/
        }
        return Code.FAILURE.toResult(this.getClass().getName() + "：方法【search(final ParamsInfo paramsInfo)】未实现");
    }

	/**
	 * 按条件分页查询列表
	 * URL:/{模块url前缀}/{pageIndex}/{pageSize}
	 * 参数：{pageIndex}当前页索引；{pageSize}每页大小；params=JSONObject
	 * @param paramsInfo ParamsInfo 参数对象
	 * @return ResultInfo<E>
	 */
	default ResultInfo<E> search(
			final int pageIndex,
			final int pageSize,
			final ParamsInfo paramsInfo) {
		{
            // 方法头注解
/*
            @GetMapping("/{pageIndex}/{pageSize}")
            @ResponseBody
*/
            // 方法接参模板，params 设置默认值为 "{}" 的目的在于，保证不会因为未传参数进不了方法
/*
            @PathVariable final int pageIndex,
            @PathVariable final int pageSize,
            @RequestParam(name = "params", required = false, defaultValue = "{}") final ParamsInfo paramsInfo) {
*/
            // 代码模板
/*
			ResultInfo<E> resultInfo = new ResultInfo<>();
			try {
			    resultInfo.setSuccess(
                    service.findPage(
                    		paramsInfo.required(false) // false 可以指定查询参数不是必须的，如果不指定false，则会校验参数是必须的，否则抛出异常
                                .parseObject(E.class),
                            Pager.builder()
                                .index(pageIndex)
                                .size(pageSize)
                                .build()
                                .sorts(E.Order.name.desc)
                    )
                );
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				resultInfo.setCode(e);
			}
			return resultInfo;
*/
		}
		return Code.FAILURE.toResult(this.getClass().getName() + "：方法【search(final int pageIndex, final int pageSize, final ParamsInfo paramsInfo)】未实现");
	}
}