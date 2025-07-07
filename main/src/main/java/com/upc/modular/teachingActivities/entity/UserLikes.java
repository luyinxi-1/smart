package com.upc.modular.teachingActivities.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author byh
 * @since 2025-07-07
 */
@Data
@Accessors(chain = true)
@TableName("user_likes")
@ApiModel(value = "UserLikes对象", description = "")
public class UserLikes implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户点赞表主键")
    @TableId("id")
    private Long id;

    @ApiModelProperty("关联类型（1：教学活动；2:回复）")
    @TableField("type")
    private Integer type;

    @ApiModelProperty("关联的教学活动或回复id")
    @TableField("correlation_id")
    private Long correlationId;

    @ApiModelProperty("点赞人")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("点赞时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;


}
