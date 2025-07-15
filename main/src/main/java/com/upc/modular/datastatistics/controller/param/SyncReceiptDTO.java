package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SyncReceiptDTO {

    @ApiModelProperty("新增成功的 uuid 列表")
    private List<String> inserted;

    @ApiModelProperty("数据库已存在（跳过）的 uuid 列表")
    private List<String> skipped;

    @ApiModelProperty("友好提示：共上传 X 条、成功 Y 条、失败 Z 条")
    private String summary;
}
