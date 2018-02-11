package com.utils.util;

import com.utils.common.entity.base.Sorts;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Objects;

/**
 * 构建分页对象
 *
 * @author Jason Xie on 2017/10/31.
 */
@Builder
public class Pager {
    public static Pager build(Pager pager) {
        if (Objects.isNull(pager)) pager = defaultPager();
        return pager;
    }
    public static Pager defaultPager() {
        return Pager.builder().build().init();
    }
    /**
     * 当前页码
     */
    private int index;
    /**
     * 每页大小
     */
    private int size;
    /**
     * 排序字段
     */
    @Getter
    private Sorts[] sorts;
    /**
     * 校验并初始化分页对象
     * @return Pager
     */
    public Pager init() {
        if (index <= 0) index = 1;
        if (size <= 0) size = 20;
        return this;
    }
    public Pager sorts(Sorts... sorts){
        this.sorts = sorts;
        return this;
    }

    /**
     * 构建 JPA 查询分页对象
     *
     * @return Pageable
     */
    public Pageable pageable() {
        final Sort sort = Sorts.jpaOrder(null, sorts);
        return Util.isEmpty(sort)
                ? PageRequest.of(index - 1, size)
                : PageRequest.of(index - 1, size, sort);
    }

    /**
     * QDSL 查询缩进量
     * @return int
     */
    public int offset() {
        return (index - 1) * size;
    }
    /**
     * QDSL 查询大小
     * @return int
     */
    public int limit() {
        return size;
    }

    @Override
    public String toString() {
        return "{" +
                "\"index\":" + index +
                ", \"size\":" + size +
                '}';
    }
}
