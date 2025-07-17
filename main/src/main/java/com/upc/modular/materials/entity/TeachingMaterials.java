package com.upc.modular.materials.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * @author mjh
 * @since 2025-07-17
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("teaching_materials")
@ApiModel(value = "TeachingMaterials对象", description = "")
public class TeachingMaterials implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableField("id")
    private Long id;

    @ApiModelProperty("教学素材名称")
    @TableField("name")
    private String name;

    @ApiModelProperty("教学素材类型")
    @TableField("type")
    private String type;

    @ApiModelProperty("描述")
    @TableField("description")
    private String description;

    @ApiModelProperty("作者id")
    @TableField("author_id")
    private Long authorId;

    @ApiModelProperty("文件路径")
    @TableField("file_path")
    private String filePath;

    @ApiModelProperty("二维码路径")
    @TableField("qrcode_path")
    private String qrcodePath;

    @ApiModelProperty("文件大小")
    @TableField("file_size")
    private Integer fileSize;

    @ApiModelProperty("上传时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;


}
