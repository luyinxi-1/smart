package com.upc.modular.textbook.param;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.upc.modular.textbook.entity.Textbook;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
public class RecentStudyReturnParam {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("教材学习进度")
    private Integer learningProgress;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("教材名称")
    @TableField("textbook_name")
    private String textbookName;

    @ApiModelProperty("教材类型，字典值信息")
    @TableField("type")
    private Long type;

    @ApiModelProperty("教材简要介绍")
    @TableField("description")
    private String description;

    @ApiModelProperty("教材出版社")
    @TableField("textbook_publishing_house")
    private String textbookPublishingHouse;

    @ApiModelProperty("教材出版时间")
    @TableField("textbook_publishing_time")
    private LocalDateTime textbookPublishingTime;

    @ApiModelProperty("教材作者（编辑该教材的教师id）")
    @TableField("textbook_author_id")
    private Long textbookAuthorId;

    @ApiModelProperty("教材版本信息")
    @TableField("textbook_version")
    private String textbookVersion;

    @ApiModelProperty("教材发布状态（1 已发布/ 0 未发布）")
    @TableField("release_status")
    private Integer releaseStatus;

    @ApiModelProperty("教材审核状态（0 未提交审核；1 审核通过 2 审核中 3 审核未通过）")
    @TableField("review_status")
    private Integer reviewStatus;

    @ApiModelProperty("保存拆分教程内容时剩余的头部的H5代码")
    @TableField(value = "h5_head_code")
    private String h5HeadCode;

    @ApiModelProperty("教材分类")
    @TableField(value = "classification")
    private Long classification;

    @ApiModelProperty("教材作者")
    @TableField(value = "author_name")
    private String authorName;

    @ApiModelProperty("责任编辑")
    @TableField(value = "executive_editor")
    private String executiveEditor;

    @ApiModelProperty("教材英文名")
    @TableField(value = "textbook_english_name")
    private String textbookEnglishName;

    @ApiModelProperty("教材版本号")
    @TableField(value = "version_number")
    private String versionNumber;

    @ApiModelProperty("教材封面")
    @TableField(value = "textbook_picture")
    private String textbookPicture;

    @ApiModelProperty("创建人（创建该教材记录的用户）")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作人（最近操作该教材记录的用户）")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;
}
