package com.upc.modular.datastatistics.controller.param;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教材数据统计概览参数
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "TextbookStatisticsOverviewParam", description = "教材数据统计概览参数")
public class TextbookStatisticsOverviewParam {

    @ExcelIgnore // 明确忽略此字段，不进行导出
    //@ExcelProperty("教材ID")
    @ApiModelProperty("教材ID")
    private Long textbookId;

    @ExcelProperty("教材名称")
    @ApiModelProperty("教材名称")
    private String textbookName;

    @ExcelProperty("阅读人数统计")
    @ApiModelProperty("阅读人数统计")
    private Long readerCount;

    @ExcelProperty("教学活动统计")
    @ApiModelProperty("教学活动统计")
    private Long teachingActivityCount;

    @ExcelProperty("素材数量")
    @ApiModelProperty("素材数量")
    private Long materialCount;

    @ExcelProperty("交流反馈数量")
    @ApiModelProperty("交流反馈数量")
    private Long communicationFeedbackCount;

    @ExcelProperty("教学思政数量")
    @ApiModelProperty("教学思政数量")
    private Long ideologicalMaterialCount;

    @ExcelProperty("习题正确率")
    @ApiModelProperty("习题正确率")
    private Double questionCorrectRate;

    @ExcelProperty("交流参与人数")
    @ApiModelProperty("交流参与人数")
    private Long communicationParticipationCount;

    @ExcelProperty("批注数量")
    @ApiModelProperty("批注数量")
    private Long annotationCount;
    
    // 各种素材类型数量统计
    @ExcelProperty("图片数量")
    @ApiModelProperty("图片数量")
    private Long imageMaterialCount;
    
    @ExcelProperty("视频数量")
    @ApiModelProperty("视频数量")
    private Long videoMaterialCount;
    
    @ExcelProperty("音频数量")
    @ApiModelProperty("音频数量")
    private Long audioMaterialCount;
    
    @ExcelProperty("3D模型数量")
    @ApiModelProperty("3D模型数量")
    private Long model3dMaterialCount;
    
    @ExcelProperty("链接数量")
    @ApiModelProperty("链接数量")
    private Long linkMaterialCount;
    
    @ExcelProperty("PPT数量")
    @ApiModelProperty("PPT数量")
    private Long pptMaterialCount;
    
    @ExcelProperty("PDF数量")
    @ApiModelProperty("PDF数量")
    private Long pdfMaterialCount;
    
    @ExcelProperty("Word数量")
    @ApiModelProperty("Word数量")
    private Long wordMaterialCount;
    
    @ExcelProperty("Excel数量")
    @ApiModelProperty("Excel数量")
    private Long excelMaterialCount;
    
    @ExcelProperty("H5页面数量")
    @ApiModelProperty("H5页面数量")
    private Long h5MaterialCount;
    
    @ExcelProperty("3D仿真数量")
    @ApiModelProperty("3D仿真数量")
    private Long simulationMaterialCount;
    
    @ExcelProperty("其他类型数量")
    @ApiModelProperty("其他类型数量")
    private Long otherMaterialCount;
}