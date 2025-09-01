package com.upc.modular.textbook.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/7/10 10:12
 */
@Data
public class TextbookAuthoritySearchParam extends PageBaseSearchParam {

    @ApiModelProperty("教材主表的id，关联教材表")
    private Long textbookId;

    @ApiModelProperty("权限类型，1表示协作者，2表示可见机构")
    private Integer authorityType;

    @ApiModelProperty("查询条件：1表示教师名称查询条件，2表示可见机构查询条件")
    private String TeacherNameOrInstituteName;
}
