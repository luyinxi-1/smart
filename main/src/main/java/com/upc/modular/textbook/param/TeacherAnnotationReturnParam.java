package com.upc.modular.textbook.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TeacherAnnotationReturnParam {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("主题")
    private String topic;

    @ApiModelProperty("批注内容")
    private String content;

    @ApiModelProperty("教材id")
    private Long textbookId;

    @ApiModelProperty("目录章节id")
    private Long catalogueId;

    @ApiModelProperty("被选中的文字")
    private String selectedContent;

    @ApiModelProperty("教师名")
    private String teacherName;

}
