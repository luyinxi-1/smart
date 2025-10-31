package com.upc.modular.homepage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 学生课程教师用户视图实体类
 * </p>
 *
 * @author byh
 * @since 2025-10-31
 */
@Data
@Accessors(chain = true)
@TableName("student_course_teacher_user_view")
@ApiModel(value = "StudentCourseTeacherUserView对象", description = "学生课程教师用户视图")
public class StudentCourseTeacherUserView implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("教师用户ID")
    private Long teacherUserId;

    @ApiModelProperty("学生用户ID")
    private Long studentUserId;

    // 可以根据视图的实际字段添加更多属性
}