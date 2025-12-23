package com.upc.modular.datastatistics.controller.param;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "时间段统计数据传输对象")
public class StatisticsDto {

    @ApiModelProperty(value = "时间段标签 (例如 '0:00-4:00')")
    private String timeSlot;

    @ApiModelProperty(value = "对应时间段的统计值")
    private double value;
}