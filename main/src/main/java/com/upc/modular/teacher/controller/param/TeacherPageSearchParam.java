package com.upc.modular.teacher.controller.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TeacherPageSearchParam extends PageBaseSearchParam {
    @ApiModelProperty("工号")
    @TableField("identity_id")
    private String identityId;

    @ApiModelProperty("身份证号")
    @TableField("idcard")
    private String idcard;

    @ApiModelProperty("姓名")
    @TableField("name")
    private String name;

    @ApiModelProperty("性别")
    @TableField("gender")
    private String gender;

    @ApiModelProperty("民族")
    @TableField("nationality")
    private String nationality;


    @ApiModelProperty("职务")
    @TableField("position")
    private String position;

    @ApiModelProperty("职称")
    @TableField("professional_title")
    private String professionalTitle;
}
