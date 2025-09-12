package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("章节掌握度信息")
public class ChapterMasteryVO {
    @ApiModelProperty("章节ID")
    private Long chapterId;
    
    @ApiModelProperty("章节名称")
    private String chapterName;
    
    @ApiModelProperty("掌握度百分比")
    private String masteryPercentage;
    
    @ApiModelProperty("掌握度显示文本")
    private String masteryDisplay;
}