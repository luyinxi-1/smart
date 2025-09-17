package com.upc.modular.questionbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
@Data
@Accessors(chain = true)
@TableName("questions_banks_list")
@ApiModel(value = "QuestionsBanksList对象", description = "")
public class QuestionsBanksList implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("题目id")
    @TableField("question_id")
    private Long questionId;

    @ApiModelProperty("题库id")
    @TableField("bank_id")
    private Long bankId;

    @ApiModelProperty("顺序")
    @TableField("sequence")
    private Integer sequence;

    @ApiModelProperty("每道题目分值")
    @TableField("score")
    private Double score;

    // 新增字段：题目类型
    @ApiModelProperty("题目类型")
    @TableField(exist = false)
    private Integer questionType;

    @ApiModelProperty("题目类型名称")
    @TableField(exist = false)
    private String questionTypeName;
    // 新增字段：题目名称/内容
    @ApiModelProperty("题目名称")
    @TableField(exist = false)
    private String questionContent;


}
