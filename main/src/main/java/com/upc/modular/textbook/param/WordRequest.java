package com.upc.modular.textbook.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
@Data
public class WordRequest {

    @ApiModelProperty("Docx文件")
    private MultipartFile file;

    @ApiModelProperty("教材id")
    private Long textbookId;
}