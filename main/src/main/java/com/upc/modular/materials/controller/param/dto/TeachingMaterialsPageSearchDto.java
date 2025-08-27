package com.upc.modular.materials.controller.param.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "素材分页查询参数")
public class TeachingMaterialsPageSearchDto extends PageBaseSearchParam {
    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("教学素材名称")
    @TableField("name")
    private String name;

    @ApiModelProperty("教学素材类型")
    @TableField("type")
    private String type;

    @ApiModelProperty("作者id")
    @TableField("author_id")
    private Long authorId;

    @ApiModelProperty("是否公开")
    @TableField("is_public")
    private Boolean isPublic;

    @ApiModelProperty("文件名")
    @TableField("file_name")
    private String fileName;

    @ApiModelProperty("文件大小(单位：M)")
    @TableField("file_size")
    private Double fileSize;

    @ApiModelProperty("文件路径")
    @TableField("file_path")
    private String filePath;

    @ApiModelProperty("封面图片路径")
    @TableField("cover_image_path")
    private String coverImagePath;

    @ApiModelProperty("素材二维码路径")
    @TableField("qrcode_path")
    private String qrcodePath;

    @ApiModelProperty("上传时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;
    }
