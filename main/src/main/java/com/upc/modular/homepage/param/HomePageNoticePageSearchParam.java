package com.upc.modular.homepage.param;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.upc.common.requestparam.PageBaseSearchParam;
import com.upc.config.time.MultiFormatLocalDateTimeDeserializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HomePageNoticePageSearchParam extends PageBaseSearchParam {

    @ApiModelProperty("公告类型")
    private Integer type;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("截止日期筛选")
    @JsonDeserialize(using = MultiFormatLocalDateTimeDeserializer.class)
    private LocalDateTime deadlineAfter;

}
