package com.upc.modular.auth.utils;

import org.springframework.beans.BeanUtils;

/**
 * 简单封装一下BeanUtils
 *
 * @author la
 */
public class MyBeanUtils {

    /**
     * @param source 复制的源
     * @param target 复制到哪个对象
     * @param <K>    源的类型
     * @param <T>    目标对象的类型
     * @return 返回复制后的对象
     */
    public static <K, T> T copy(K source, T target) {
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
