package com.upc.modular.questionbank.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author la
 * @since 2025-07-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("student_final_grade")
@ApiModel(value = "StudentFinalGrade对象", description = "")
public class StudentFinalGrade implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId("id")
    private Long id;

    @ApiModelProperty("学生ID")
    @TableField("student_id")
    private Long studentId;

    @ApiModelProperty("题库ID")
    @TableField("bank_id")
    private Long bankId;

    @ApiModelProperty("最终有效成绩")
    @TableField("final_score")
    private Double finalScore;

    @ApiModelProperty("产生此最终成绩的答卷记录ID")
    @TableField("record_id")
    private Long recordId;

    @ApiModelProperty("更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;


}
