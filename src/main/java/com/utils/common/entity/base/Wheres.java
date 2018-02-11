package com.utils.common.entity.base;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Jason Xie on 2018/1/12.
 */
public class Wheres {
    private final List<Predicate> predicates;

    private Wheres() {
        predicates = new ArrayList<>();
    }

    public static Wheres build() {
        return new Wheres();
    }

    public Wheres add(Object value, BooleanExpression... expression) {
        return add(Objects.nonNull(value), expression);
    }

    public Wheres add(boolean check, BooleanExpression... expression) {
        if (check) {
            if (0 == expression.length) predicates.add(expression[0]);
            else predicates.add(Stream.of(expression).reduce((s, v) -> s.or(v)).orElse(null));
        }
        return this;
    }

    public Wheres add(boolean value, Supplier<Predicate> supplier) {
        if (value)
            predicates.add(supplier.get());
        return this;
    }

    public List<Predicate> get() {
        return predicates;
    }

    public Predicate[] toArray() {
        return predicates.toArray(new Predicate[predicates.size()]);
    }
}
