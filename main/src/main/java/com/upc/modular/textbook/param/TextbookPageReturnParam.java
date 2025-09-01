package com.upc.modular.textbook.param;

import com.upc.modular.textbook.entity.Textbook;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TextbookPageReturnParam extends Textbook {

    @ApiModelProperty("教材作者姓名")
    private String textbookAuthorName;

    @ApiModelProperty("教材类型名称")
    private String typeName;

    @ApiModelProperty("创建人姓名")
    private String creatorName;

    @ApiModelProperty("审核表id")
    private Long reviewId;
}
