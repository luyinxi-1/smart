package com.upc.modular.datastatistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("student_statistics_data")
@ApiModel(value = "StudentStatisticsData对象", description = "")
public class StudentStatisticsData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户id")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("图片url")
    @TableField("pic_url")
    private String picUrl;

    @ApiModelProperty("阅读时间")
    @TableField("reading_time")
    private Long readingTime;

    @ApiModelProperty("阅读书籍")
    @TableField("reading_book")
    private Long readingBook;

    @ApiModelProperty("更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
