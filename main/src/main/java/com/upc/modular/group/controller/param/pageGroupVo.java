package com.upc.modular.group.controller.param;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.modular.group.entity.Group;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data // <-- 3. Add this annotation
@EqualsAndHashCode(callSuper = true) // <--
public class pageGroupVo extends Group {

    @ApiModelProperty("创建人")
    private String creatorName;
}
