package com.upc.modular.textbook.param;

import com.upc.modular.textbook.entity.Textbook;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TextbookCenterPageReturnParam extends Textbook {
    @ApiModelProperty("该学生参与的活动数量")
    private Integer activityCount;
}
