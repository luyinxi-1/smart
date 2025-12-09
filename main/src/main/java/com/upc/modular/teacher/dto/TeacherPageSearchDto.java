package com.upc.modular.teacher.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TeacherPageSearchDto extends PageBaseSearchParam {

    @ApiModelProperty("工号")
    private String identityId;

    @ApiModelProperty("身份证号")
    private String idcard;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("性别")
    private String gender;

    @ApiModelProperty("民族")
    private String nationality;

    @ApiModelProperty("职务")
    private String position;

    @ApiModelProperty("职称")
    private String professionalTitle;

    @ApiModelProperty("学历（0：本科，1：硕士，2：博士）")
    private Integer educationalBackground;

    @ApiModelProperty("是否为党员（0为否，1为是）")
    private Integer isPartyNumber;

    @ApiModelProperty("状态")
    private Integer status;

    /**
     * 前端下拉选中的机构ID，对应你说的 detailTypeOption
     */
    private Long detailTypeOption;


}
