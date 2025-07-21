package com.upc.modular.group.controller.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.modular.group.entity.Group;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class GetMyClasssReturnParam extends Group {

    @ApiModelProperty("机构名称")
    private String institutionName;

    @ApiModelProperty("教师姓名")
    private String teacherName;

}
