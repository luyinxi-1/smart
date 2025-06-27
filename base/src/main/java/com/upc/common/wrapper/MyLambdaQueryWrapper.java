package com.upc.common.wrapper;

import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MyLambdaQueryWrapper<T> extends LambdaQueryWrapper<T> {

    public MyLambdaQueryWrapper() {
        this((T) null);
    }

    public MyLambdaQueryWrapper(T entity) {
        super.setEntity(entity);
        super.initNeed();
    }

    public MyLambdaQueryWrapper(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        super.initNeed();
    }

    MyLambdaQueryWrapper(T entity, Class<T> entityClass, AtomicInteger paramNameSeq,
                         Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments, SharedString paramAlias,
                         SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.paramNameSeq = paramNameSeq;
        this.paramNameValuePairs = paramNameValuePairs;
        this.expression = mergeSegments;
        this.paramAlias = paramAlias;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
    }

    @Override
    public LambdaQueryWrapper<T> eq(SFunction<T, ?> column, Object val) {
        return super.eq(isNotEmpty(val), column, val);
    }

    @Override
    public LambdaQueryWrapper<T> ne(SFunction<T, ?> column, Object val) {
        return super.ne(isNotEmpty(val), column, val);
    }

    @Override
    public LambdaQueryWrapper<T> gt(SFunction<T, ?> column, Object val) {
        return super.gt(isNotEmpty(val), column, val);
    }

    @Override
    public LambdaQueryWrapper<T> ge(SFunction<T, ?> column, Object val) {
        return super.ge(column, val);
    }

    @Override
    public LambdaQueryWrapper<T> lt(SFunction<T, ?> column, Object val) {
        return super.lt(isNotEmpty(val), column, val);
    }

    @Override
    public LambdaQueryWrapper<T> le(SFunction<T, ?> column, Object val) {
        return super.le(isNotEmpty(val), column, val);
    }

    @Override
    public LambdaQueryWrapper<T> between(SFunction<T, ?> column, Object val1, Object val2) {
        return super.between(isNotEmpty(val1) && isNotEmpty(val2), column, val1, val2);
    }

    @Override
    public LambdaQueryWrapper<T> notBetween(SFunction<T, ?> column, Object val1, Object val2) {
        return super.notBetween(isNotEmpty(val1) && isNotEmpty(val2), column, val1, val2);
    }

    @Override
    public LambdaQueryWrapper<T> like(SFunction<T, ?> column, Object val) {
        return super.like(isNotEmpty(val), column, val);
    }

    @Override
    public LambdaQueryWrapper<T> notLike(SFunction<T, ?> column, Object val) {
        return super.notLike(isNotEmpty(val), column, val);
    }

    @Override
    public LambdaQueryWrapper<T> likeLeft(SFunction<T, ?> column, Object val) {
        return super.likeLeft(isNotEmpty(val), column, val);
    }

    @Override
    public LambdaQueryWrapper<T> likeRight(SFunction<T, ?> column, Object val) {
        return super.likeRight(isNotEmpty(val), column, val);
    }


    @Override
    public LambdaQueryWrapper<T> in(SFunction<T, ?> column, Collection<?> coll) {
        return super.in(collIsNotEmpty(coll), column, coll);
    }

    @Override
    public LambdaQueryWrapper<T> in(SFunction<T, ?> column, Object... values) {
        return super.in(isNotEmpty(values), column, values);
    }

    @Override
    public LambdaQueryWrapper<T> notIn(SFunction<T, ?> column, Collection<?> coll) {
        return super.notIn(collIsNotEmpty(coll), column, coll);
    }

    @Override
    public LambdaQueryWrapper<T> notIn(SFunction<T, ?> column, Object... value) {
        return super.notIn(isNotEmpty(value), column, value);
    }

    @Override
    public LambdaQueryWrapper<T> inSql(SFunction<T, ?> column, String inValue) {
        return super.inSql(isNotEmpty(inValue), column, inValue);
    }

    @Override
    public LambdaQueryWrapper<T> gtSql(SFunction<T, ?> column, String inValue) {
        return super.gtSql(isNotEmpty(inValue), column, inValue);
    }

    @Override
    public LambdaQueryWrapper<T> geSql(SFunction<T, ?> column, String inValue) {
        return super.geSql(isNotEmpty(inValue), column, inValue);
    }

    @Override
    public LambdaQueryWrapper<T> ltSql(SFunction<T, ?> column, String inValue) {
        return super.ltSql(isNotEmpty(inValue), column, inValue);
    }

    @Override
    public LambdaQueryWrapper<T> leSql(SFunction<T, ?> column, String inValue) {
        return super.leSql(isNotEmpty(inValue), column, inValue);
    }

    @Override
    public LambdaQueryWrapper<T> notInSql(SFunction<T, ?> column, String inValue) {
        return super.notInSql(isNotEmpty(inValue), column, inValue);
    }

    @Override
    public LambdaQueryWrapper<T> orderByAsc(List<SFunction<T, ?>> columns) {
        return super.orderByAsc(columns);
    }

    @Override
    public LambdaQueryWrapper<T> having(String sqlHaving, Object... params) {
        return super.having(isNotEmpty(params), sqlHaving, params);
    }

    //    判断对象是不是空对象或者是空串，是返回false
    private boolean isNotEmpty(Object... val) {
        for (Object obj : val) {
            if (("".equals(obj) || obj == null)) {
                return false;
            }
        }
        return true;
    }

    //判断集合是否不为空，不为空返回true
    private boolean collIsNotEmpty(@Nullable Collection<?> collection) {
        return !(collection == null || collection.isEmpty());
    }
}

