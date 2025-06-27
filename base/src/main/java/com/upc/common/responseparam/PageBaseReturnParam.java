package com.upc.common.responseparam;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageBaseReturnParam<T> {
    @ApiModelProperty("记录的条数")
    private Long total;

    @ApiModelProperty("当前页")
    private Long pageNo;

    @ApiModelProperty("数据")
    private List<T> data;

    public static <T> PageBaseReturnParam<T> ok(Long total, Long current, List<T> data) {
        return new PageBaseReturnParam<>(total, current, data);
    }

    public static <T> PageBaseReturnParam<T> ok(Page<T> page) {
        return new PageBaseReturnParam<>(page.getTotal(), page.getCurrent(), page.getRecords());
    }

    public static <T> PageBaseReturnParam<T> ok(IPage<T> page) {
        return new PageBaseReturnParam<>(page.getTotal(), page.getCurrent(), page.getRecords());
    }

    /**
     * 不符合查询条件时使用，构造哟个空的返回类
     *
     * @return 空返回类
     */
    public static PageBaseReturnParam<Void> noneData() {
        return new PageBaseReturnParam<>(0L, 1L, Collections.emptyList());
    }
}
