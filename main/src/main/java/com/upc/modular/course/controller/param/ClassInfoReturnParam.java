package com.upc.modular.course.controller.param;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ClassInfoReturnParam {

    @ApiModelProperty("班级名称")
    private String name;

    @ApiModelProperty("年级")
    private Integer grade;

    @ApiModelProperty("学生数量")
    private Long studentCount;
}
