package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 教师班级数量统计返回参数
 */
@Data
@ApiModel("教师班级数量统计返回参数")
public class TeacherClassCountReturnParam {
    
    @ApiModelProperty("班级总数")
    private Integer classCount;
    
    @ApiModelProperty("班级列表")
    private List<ClassInfo> classList;
    
    @Data
    @ApiModel("班级信息")
    public static class ClassInfo {
        @ApiModelProperty("班级ID")
        private Long classId;
        
        @ApiModelProperty("班级名称")
        private String className;
    }
}

