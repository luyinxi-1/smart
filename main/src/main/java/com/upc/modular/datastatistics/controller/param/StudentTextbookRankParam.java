package com.upc.modular.datastatistics.controller.param;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
@Data
@Accessors(chain = true)
@ApiModel(value = "StudentTextbookRankParam", description = "学生教材阅读排行榜")
public class StudentTextbookRankParam implements Serializable {
    @ApiModelProperty("教材id")
    private Long textbook_id;

    @ApiModelProperty("教材名称")
    private String textbook_name;

    @ApiModelProperty("阅读时长")
    private long read_time;
}
