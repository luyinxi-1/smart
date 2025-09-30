package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel(value = "StudentTextbookSituationReturnParam", description = "统计指定书籍阅读情况")
public class StudentTextbookSituationReturnParam {
    @ApiModelProperty("学习次数")
    private Long studyCount;

    @ApiModelProperty("最后阅读的目录id")
    private Long lastReadingCatalogueId;

    @ApiModelProperty("阅读过的目录id列表")
    private List<Long> readingCatalogueIdList;

	@ApiModelProperty("阅读总时长(分钟)")
	private Long readingDurationMinutes;

}
