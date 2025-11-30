package com.upc.modular.materials.controller.param.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/11/30 15:37
 */
@Data
public class TeachingMaterialsInsertMaterialsReturnParam {

    @ApiModelProperty("素材id")
    private Long materialId;

    @ApiModelProperty("文件路径")
    @TableField("file_path")
    private String filePath;

    @ApiModelProperty("文件名")
    @TableField("file_name")
    private String fileName;
}
