package com.utils.common.entity.base;

import com.querydsl.core.types.OrderSpecifier;
import com.utils.util.Util;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 查询排序对象
 *
 * @author Jason Xie on 2017/12/21.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Accessors(fluent = true)
public class Sorts {
    public static OrderSpecifier[] qdslOrder(final Sorts defaultSort, Sorts... sorts) {
        if (Util.isEmpty(sorts)) return new OrderSpecifier[]{defaultSort.qdsl()};
        final List<OrderSpecifier> list = Stream.of(sorts).map(v -> v.qdsl()).collect(Collectors.toList());
        return list.toArray(new OrderSpecifier[list.size()]);
    }

    public static Sort jpaOrder(final Sorts defaultSort, Sorts... sorts) {
        return Util.isEmpty(sorts)
                ? (Util.isEmpty(defaultSort) ? null : defaultSort.jpa())
                : Stream.of(sorts).map(v -> v.jpa()).reduce((s, v) -> s.and(v)).orElse(null);
    }

    private OrderSpecifier qdsl;
    private Sort jpa;
}
