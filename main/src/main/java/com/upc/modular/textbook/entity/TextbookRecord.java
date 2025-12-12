package com.upc.modular.textbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("textbook_record")
public class TextbookRecord {

    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("教材ID")
    private Long textbookId;
    @ApiModelProperty("教材名称")
    @TableField("textbook_name")
    private String textbookName;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("创建时间")
    private LocalDateTime addDatetime;

    /**
     * 状态：1新增，2修改，3删除
     */
    @ApiModelProperty("状态：1新增，2修改，3删除")
    private Long status;

    @ApiModelProperty("章节ID")
    private Long catalogId;

    @ApiModelProperty("章节名称")
    private String catalogName;
}

