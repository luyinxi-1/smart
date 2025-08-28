package com.upc.utils;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public class CreatePage {

    /**
     * 获取分页信息
     *
     * @param list 要分页的完整列表
     * @param <T> 列表中元素的类型
     * @param current 当前页码（从 1 开始）
     * @param size 每页大小
     * @return 分页对象
     */
    public static <T> Page<T> createPage(List<T> list, int current, int size) {
        // 创建分页对象
        Page<T> page = new Page<>(current, size);

        // 计算分页的开始和结束位置
        int start = (current - 1) * size;
        int end = Math.min(start + size, list.size());

        // 防止 start 超出范围
        if (start >= list.size()) {
            return page;
        }
        // 获取分页的子列表
        List<T> pageList = list.subList(start, end);
        // 设置分页信息
        page.setRecords(pageList);
        page.setTotal(list.size());

        return page;
    }

    public static <T> Page<T> createPage(List<T> list, Long current, Long size) {
        // 创建分页对象
        Page<T> page = new Page<>(current, size);

        // 计算分页的开始和结束位置
        Long start = (current - 1) * size;
        Long end = Math.min(start + size, list.size());

        // 防止 start 超出范围
        if (start >= list.size()) {
            return page;
        }
        // 获取分页的子列表
        List<T> pageList = list.subList(start.intValue(), end.intValue());
        // 设置分页信息
        page.setRecords(pageList);
        page.setTotal(list.size());

        return page;
    }
}
