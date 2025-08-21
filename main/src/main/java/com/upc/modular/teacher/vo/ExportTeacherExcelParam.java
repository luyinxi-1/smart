package com.upc.modular.teacher.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ExcelIgnoreUnannotated
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ExportTeacherExcelParam {

    @ApiModelProperty("姓名")
    @ExcelProperty("姓名")
    private String name;

    @ApiModelProperty("工号")
    @ExcelProperty("工号")
    private String identityId;

    @ApiModelProperty("身份证号")
    @ExcelProperty("身份证号")
    private String idcard;

    @ApiModelProperty("性别")
    @ExcelProperty("性别")
    private String gender;

    @ApiModelProperty("民族")
    @ExcelProperty("民族")
    private String nationality;

    @ApiModelProperty("生日")
    @ExcelProperty("生日")
    private String birthday;

    @ApiModelProperty("职务")
    @ExcelProperty("职务")
    private String position;

    @ApiModelProperty("职称")
    @ExcelProperty("职称")
    private String professionalTitle;

    @ApiModelProperty("邮箱")
    @ExcelProperty("邮箱")
    private String email;

    @ApiModelProperty("电话")
    @ExcelProperty("电话")
    private String phone;

    @ApiModelProperty("介绍")
    @ExcelProperty("介绍")
    private String introduction;

    @ApiModelProperty("学历（本科，硕士，博士）")
    @ExcelProperty("学历")
    private String educationalBackground;

    @ApiModelProperty("是否为党员（否，是）")
    @ExcelProperty("党员")
    private String isPartyNumber;

    @ApiModelProperty("教学年限")
    @ExcelProperty("教学年限")
    private String teachingYears;
}
