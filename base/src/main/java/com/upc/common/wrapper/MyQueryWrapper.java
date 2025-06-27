package com.upc.common.wrapper;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class MyQueryWrapper<T> extends AbstractWrapper<T, String, MyQueryWrapper<T>>
        implements Query<MyQueryWrapper<T>, T, String> {

    private final SharedString sqlSelect = new SharedString();

    public MyQueryWrapper() {
        this(null);
    }

    public MyQueryWrapper(T entity) {
        super.setEntity(entity);
        super.initNeed();
    }

    public MyQueryWrapper(T entity, String... columns) {
        super.setEntity(entity);
        super.initNeed();
        this.select(columns);
    }



    private MyQueryWrapper(T entity, Class<T> entityClass, AtomicInteger paramNameSeq,
                           Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments,
                           SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.paramNameSeq = paramNameSeq;
        this.paramNameValuePairs = paramNameValuePairs;
        this.expression = mergeSegments;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
    }

    /**
     *
     * @return MyLambdaQueryWrapper对象
     */
    public MyLambdaQueryWrapper<T> myLambda() {
        return new MyLambdaQueryWrapper<>(getEntity(), getEntityClass(), paramNameSeq, paramNameValuePairs,
                expression, paramAlias, lastSql, sqlComment, sqlFirst);
    }
    @Override
    protected MyQueryWrapper<T> instance() {
        return new MyQueryWrapper<>(getEntity(), getEntityClass(), paramNameSeq, paramNameValuePairs, new MergeSegments(),
                SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
    }

    @Override
    public MyQueryWrapper<T> select(String... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            this.sqlSelect.setStringValue(String.join(StringPool.COMMA, columns));
        }
        return typedThis;
    }

    @Override
    public MyQueryWrapper<T> select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
        super.setEntityClass(entityClass);
        this.sqlSelect.setStringValue(TableInfoHelper.getTableInfo(getEntityClass()).chooseSelect(predicate));
        return typedThis;
    }


    @Override
    public void clear() {
        super.clear();
        sqlSelect.toNull();
    }

    @Override
    public MyQueryWrapper<T> eq(String column, Object val) {
        return super.eq(isNotEmpty(val), column, val);
    }

    @Override
    public MyQueryWrapper<T> ne(String column, Object val) {
        return super.ne(isNotEmpty(val), column, val);
    }

    @Override
    public MyQueryWrapper<T> gt(String column, Object val) {
        return super.gt(isNotEmpty(val), column, val);
    }

    @Override
    public MyQueryWrapper<T> ge(String column, Object val) {
        return super.ge(isNotEmpty(val), column, val);
    }

    @Override
    public MyQueryWrapper<T> lt(String column, Object val) {
        return super.lt(isNotEmpty(val), column, val);
    }

    @Override
    public MyQueryWrapper<T> le(String column, Object val) {
        return super.le(isNotEmpty(val), column, val);
    }

    @Override
    public MyQueryWrapper<T> like(String column, Object val) {
        return super.like(isNotEmpty(val), column, val);
    }

    @Override
    public MyQueryWrapper<T> notLike(String column, Object val) {
        return super.notLike(isNotEmpty(val), column, val);
    }

    @Override
    public MyQueryWrapper<T> likeLeft(String column, Object val) {
        return super.likeLeft(isNotEmpty(val), column, val);
    }

    @Override
    public MyQueryWrapper<T> likeRight(String column, Object val) {
        return super.likeRight(isNotEmpty(val), column, val);
    }

    @Override
    public MyQueryWrapper<T> between(String column, Object val1, Object val2) {
        return super.between(isNotEmpty(val1, val2), column, val1, val2);
    }

    @Override
    public MyQueryWrapper<T> in(String column, Object... values) {
        return super.in(isNotEmpty(values), column, values);
    }

    @Override
    public MyQueryWrapper<T> in(String column, Collection<?> coll) {
        return super.in(CollectionUtils.isNotEmpty(coll), column, coll);
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
}
