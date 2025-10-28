package com.upc.modular.textbook.param;

import com.upc.modular.textbook.entity.Textbook;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "TextbookHotnessDto", description = "教材热度排行信息")
public class TextbookHotnessDto extends Textbook {

    @ApiModelProperty(value = "收藏数量（热度）")
    private Long favoritesCount;
}
