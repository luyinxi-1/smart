package com.upc.modular.teachingactivities.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DiscussionTopicSecondReturnParam {
    @ApiModelProperty("讨论关联的教材名称")
    private String textbookName;

    @ApiModelProperty("讨论关联的教材目录名称")
    private String textbookCatalogName;

    @ApiModelProperty("话题的类型")
    private String type;

    @ApiModelProperty("创建人姓名")
    private String nickName;


}
