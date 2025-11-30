package com.upc.modular.materials.controller.param.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@ApiModel(value = "教学素材返回对象")
public class TeachingMaterialsReturnVo {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("教学素材名称")
    private String name;

    @ApiModelProperty("教学素材类型")
    private String type;

    @ApiModelProperty("作者名")
    private String authorName;

    @ApiModelProperty("是否公开")
    private Boolean isPublic;

    @ApiModelProperty("文件名")
    private String fileName;

    @ApiModelProperty("封面图片路径")
    private String coverImagePath;

    @ApiModelProperty("素材二维码路径")
    private String qrcodePath;

    @ApiModelProperty("上传时间")
    private LocalDateTime addDatetime;

    @ApiModelProperty("文件路径")
    private String filePath;
    /**
     * 是否为当前用户创建
     * true: 是, false: 否
     */
    @ApiModelProperty("是否为本人上传")
    private Boolean isCreator;
    /**
     * 教材ID
     */
    @ApiModelProperty("已绑定的教材ID")
    private Long textbookId;

    /**
     * 教材名称
     */
    @ApiModelProperty("已绑定的教材名称")
    private String textbookName;
    
    /**
     * 章节ID
     */
    @ApiModelProperty("已绑定的章节ID")
    private Long chapterId;
    
    /**
     * 备用章节ID
     */
    @ApiModelProperty("备用章节ID")
    private Long chapterId2;
    
    /**
     * 章节名称
     */
    @ApiModelProperty("已绑定的章节名称")
    private String chapterName;
}