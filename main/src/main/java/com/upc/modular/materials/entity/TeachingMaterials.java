package com.upc.modular.materials.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author mjh
 * @since 2025-07-17
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("teaching_materials")
@ApiModel(value = "TeachingMaterials对象", description = "")
public class TeachingMaterials implements Serializable {

    //public static final List<String> SUPPORTED_TYPES = Arrays.asList("image", "imageSet", "video", "audio", "3DModel", "link", "file", "other");
    public static final List<String> SUPPORTED_TYPES = Arrays.asList("image", "imageSet", "video", "audio", "3DModel",
            "link", "ppt","pdf","word", "excel","H5","simulation","other" );
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("教学素材名称")
    @TableField("name")
    private String name;

    //  image imageSet video audio 3DModel link pdf word excel ppt other
//  图片   图集      视频  音频   3D模型   链接 pdf word excel ppt 其他类型
    @ApiModelProperty("素材类型：image imageSet video audio 3DModel link pdf word excel ppt h5 3D仿真 other")
    @TableField("type")
    private String type;

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

    @ApiModelProperty("创建人-作者id")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("上传时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作人")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;


}
