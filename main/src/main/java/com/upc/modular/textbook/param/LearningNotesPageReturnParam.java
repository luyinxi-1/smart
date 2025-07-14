package com.upc.modular.textbook.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.modular.textbook.entity.LearningNotes;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class LearningNotesPageReturnParam extends LearningNotes {

    @ApiModelProperty("关联的教材名")
    private String textbookName;

    @ApiModelProperty("关联的目录名")
    private String catalogName;

}
