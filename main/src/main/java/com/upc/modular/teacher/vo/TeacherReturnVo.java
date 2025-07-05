package com.upc.modular.teacher.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.upc.modular.teacher.entity.Teacher;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TeacherReturnVo extends Teacher {

    @ApiModelProperty("机构名称")
    private String institutionName;

    @ApiModelProperty("机构id")
    private Long institutionId;

    @ApiModelProperty("角色id")
    private Long roleId;

    @ApiModelProperty("角色名称")
    private String roleName;


}
