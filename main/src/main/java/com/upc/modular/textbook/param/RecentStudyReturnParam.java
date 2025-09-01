package com.upc.modular.textbook.param;

import com.upc.modular.textbook.entity.Textbook;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RecentStudyReturnParam extends Textbook {
    @ApiModelProperty("教材学习进度")
    private Integer learningProgress;
}
