package com.upc.modular.materials.controller.param.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TeachingMaterialsSaveOrUpdateParam {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("教学素材名称")
    @TableField("name")
    private String name;

    //  image imageSet video audio 3DModel link pdf word excel ppt other
//  图片   图集      视频  音频   3D模型   链接 pdf word excel ppt 其他类型
    @ApiModelProperty("素材类型：image imageSet video audio 3DModel link pdf word excel ppt other")
    private String type;

    @ApiModelProperty("是否公开")
    private Boolean isPublic;

    @ApiModelProperty("文件路径（仅供链接用）")
    private String filePath;

    @ApiModelProperty("封面图片路径")
    @TableField("cover_image_path")
    private String coverImagePath;

    @ApiModelProperty("素材二维码路径")
    @TableField("qrcode_path")
    private String qrcodePath;

}
