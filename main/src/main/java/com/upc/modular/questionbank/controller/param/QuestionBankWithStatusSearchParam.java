package com.upc.modular.questionbank.controller.param;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Accessors(chain = true)
public class QuestionBankWithStatusSearchParam {
    @ApiModelProperty("教材ID")
    private Long textbookId;

    @ApiModelProperty("章节ID（精确查询）")
    private Long textbookCatalogId;

    @ApiModelProperty("题库名称（模糊查询）")
    private String teachingQuestionBankName;

    // 由后端填充，不对外暴露
    @JsonIgnore
    private Long teacherId;
}
