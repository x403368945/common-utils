package com.utils.common.entity;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 查询条件接口定义
 *
 * @author Jason Xie 2018-1-12
 */
public interface IWhere {

    /**
     * 构建 QueryDSL 查询条件，执行 findList 和 findPage 时使用
     * @return {@link IWhere.QdslWheres}
     */
    QdslWheres qdslWhere();

    /**
     * QueryDSL 查询条件集合
     */
    class QdslWheres {
        private final List<Predicate> predicates;

        private QdslWheres() {
            predicates = new ArrayList<>();
        }

        public static QdslWheres build() {
            return new QdslWheres();
        }

        /**
         * where 条件拼接
         * @param or {@link IWhere.QdslWheres.Or}
         * @return QdslWheres
         */
        public QdslWheres and(@Nonnull Or or) {
            if (or.notEmpty()) predicates.add(or.toPredicate());
            return this;
        }
        /**
         * where 条件拼接
         * @param predicate {@link com.querydsl.core.types.Predicate}
         * @return QdslWheres
         */
        public QdslWheres and(@Nonnull Predicate predicate) {
            predicates.add(predicate);
            return this;
        }
        /**
         * where 条件拼接
         * @param value    Object value非空时，执行supplier.get()获得查询条件
         * @param supplier Supplier<Predicate>
         * @return QdslWheres
         */
        public QdslWheres and(Object value, Supplier<Predicate> supplier) {
            return and(Objects.nonNull(value), supplier);
        }
        /**
         * where 条件拼接
         * @param hasTrue    boolean hasTrue为true时，执行supplier.get()获得查询条件
         * @param supplier Supplier<Predicate>
         * @return QdslWheres
         */
        public QdslWheres and(boolean hasTrue, Supplier<Predicate> supplier) {
            if (hasTrue)
                predicates.add(supplier.get()); // Optional.ofNullable(supplier.get()).map(v -> predicates.add(v));
            return this;
        }

        public boolean isEmpty() {
            return 0 == predicates.size();
        }

        public boolean notEmpty() {
            return !isEmpty();
        }

        public List<Predicate> get() {
            return predicates;
        }

        public Predicate[] toArray() {
//            return predicates.stream().toArray(p->new Predicate[p]);
            return predicates.toArray(new Predicate[predicates.size()]);
        }

        public final static class Or {
            private final List<BooleanExpression> expressions;

            private Or() {
                expressions = new ArrayList<>();
            }

            public static Or build() {
                return new Or();
            }

            public Or or(Object value, @Nonnull Supplier<BooleanExpression> supplier) {
                return or(Objects.nonNull(value), supplier);
            }

            public Or or(boolean hasTrue, @Nonnull Supplier<BooleanExpression> supplier) {
                if (hasTrue)
                    expressions.add(supplier.get()); // Optional.ofNullable(supplier.get()).map(v -> expressions.add(v));
                return this;
            }

            public Or add(boolean hasTrue, Supplier<BooleanExpression> supplier) {
                if (hasTrue)
                    expressions.add(supplier.get());
                return this;
            }

            public boolean isEmpty() {
                return 0 == expressions.size();
            }

            public boolean notEmpty() {
                return !isEmpty();
            }

            public List<BooleanExpression> get() {
                return expressions;
            }

            public Predicate toPredicate() {
                if (1 == expressions.size()) return expressions.get(0);
                else if (expressions.size() > 1) return expressions.stream().reduce((s, v) -> s.or(v)).orElse(null);
                return null;
            }
        }
    }

}