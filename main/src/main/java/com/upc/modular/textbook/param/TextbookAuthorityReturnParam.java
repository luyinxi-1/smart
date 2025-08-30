package com.upc.modular.textbook.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.textbook.entity.TextbookAuthority;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/8/29 20:29
 */
@Data
public class TextbookAuthorityReturnParam extends TextbookAuthority {

    @ApiModelProperty("教师信息")
    private Teacher teacher;
}
