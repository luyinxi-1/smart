package com.upc.modular.homepage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 教材综合视图实体类
 * </p>
 *
 * @author byh
 * @since 2025-10-31
 */
@Data
@Accessors(chain = true)
@TableName("textbook_comprehensive_view")
@ApiModel(value = "TextbookComprehensiveView对象", description = "教材综合视图")
public class TextbookComprehensiveView implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("教师用户ID")
    private Long teacherUserId;

    @ApiModelProperty("学生用户ID")
    private Long studentUserId;

    // 可以根据视图的实际字段添加更多属性
}