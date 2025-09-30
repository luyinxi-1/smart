package com.upc.modular.materials.controller.param.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@Accessors(chain = true)
@ApiModel(value = "素材分页查询参数")
public class TeachingMaterialsPageSearchDto extends PageBaseSearchParam {

    @ApiModelProperty("作者id(仅管理员查询有效)")
    private Long authorId;

    @ApiModelProperty(value = "是否只筛选未绑定的素材")
    private Boolean unboundOnly;

    @ApiModelProperty("模糊查询参数 教学素材名称")
    private String name;

    @ApiModelProperty("教学素材类型")
    private String type;

    @ApiModelProperty("是否公开")
    private Boolean isPublic;
}
