package com.upc.modular.institution.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author byh
 * @since 2025-07-01
 */
@Data
@Accessors(chain = true)
@TableName("institution")
@ApiModel(value = "Institution对象", description = "")
public class Institution implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("机构名称")
    @TableField("institution_name")
    private String institutionName;

    @ApiModelProperty("父级机构id")
    @TableField("father_institution_id")
    private Long fatherInstitutionId;

    @ApiModelProperty("机构编码")
    @TableField("institution_code")
    private String institutionCode;

    @ApiModelProperty("机构级别")
    @TableField("institution_grade")
    private Integer institutionGrade;

    @ApiModelProperty("介绍")
    @TableField("introduction")
    private String introduction;

    @ApiModelProperty("排序")
    @TableField("sort")
    private Integer sort;

    @ApiModelProperty("图标")
    @TableField("pic_url")
    private String picUrl;

    @ApiModelProperty("创建者")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作者")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;


}
