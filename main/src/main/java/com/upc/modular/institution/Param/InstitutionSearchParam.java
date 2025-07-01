package com.upc.modular.institution.Param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/7/1 20:15
 */
@Data
public class InstitutionSearchParam {

    @ApiModelProperty("机构名称")
    @TableField("institution_name")
    private String institutionName;

    @ApiModelProperty("机构编码")
    @TableField("institution_code")
    private String institutionCode;

    @ApiModelProperty("机构级别")
    @TableField("institution_grade")
    private Integer institutionGrade;

}
