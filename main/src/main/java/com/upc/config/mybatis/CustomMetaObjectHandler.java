package com.upc.config.mybatis;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.log.Log;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

import com.upc.common.utils.UserUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;

import java.time.LocalDateTime;

import static com.upc.constant.MybatisConst.*;

public class CustomMetaObjectHandler implements MetaObjectHandler {
    private static final Log log = Log.get();



    /**
     * 插入记录时自动填充字段
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        try {
            //为空则设置createUser(BaseEntity)
            Object createUser = metaObject.getValue(CREATOR);
            if (ObjectUtil.isNull(createUser)) {
                setFieldValByName(CREATOR, this.getUser(), metaObject);
            }
//            log.info("测试1：{}",LocalDateTime.now());
            //为空则设置createTime（BaseEntity)
            Object createTime = metaObject.getValue(ADD_DATE_TIME);
            if (ObjectUtil.isNull(createTime)) {
//                log.info("测试2：{}",LocalDateTime.now());
                setFieldValByName(ADD_DATE_TIME, LocalDateTime.now(), metaObject);
            }
        } catch (ReflectionException e) {
            log.warn(">>> CustomMetaObjectHandler处理过程中无相关字段，不做处理");
        }
    }

    /**
     * 更新字段时自动填充字段
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            Long userId = this.getUser();
            setFieldValByName(OPERATOR, userId, metaObject);

            LocalDateTime now = LocalDateTime.now();
            setFieldValByName(OPERATION_DATE_TIME, now, metaObject);

        } catch (ReflectionException e) {
            log.warn(">>> CustomMetaObjectHandler updateFill 过程中无相关字段，不做处理");
        }
    }



    /**
     * 获取用户名
     */
    private Long getUser() {
        // 如果 UserUtils.get() 返回的对象为 null，则返回null
        if (UserUtils.get() != null) {
            log.info("当前用户为：{}", UserUtils.get());
            return UserUtils.get().getId();
        } else {
            log.warn("UserUtils.get() 返回 null，无法获取用户名");
            return null; // 返回null
        }
    }
}
