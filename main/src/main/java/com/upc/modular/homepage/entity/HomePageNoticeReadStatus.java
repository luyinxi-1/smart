package com.upc.modular.homepage.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.upc.config.time.MultiFormatLocalDateTimeDeserializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author byh
 * @since 2025-10-31
 */
@Data
@Accessors(chain = true)
@TableName("home_page_notice_read_status")
public class HomePageNoticeReadStatus {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("通知公告id")
    @TableField("notice_id")
    private Long notice_id;

    @ApiModelProperty("用户id")
    @TableField("user_id")
    private Long user_id;

    @ApiModelProperty("阅读状态（0:未读, 1:已读）")
    @TableField("read_status")
    private Integer read_status;

    @ApiModelProperty("阅读时间")
    @TableField("read_time")
    private LocalDateTime read_time;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private LocalDateTime create_time;
}