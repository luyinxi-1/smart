package com.upc.modular.textbook.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class UserFavoritesPageSearch extends PageBaseSearchParam {

    @ApiModelProperty("教材分类")
    private Long classification = null;

    @ApiModelProperty("书籍名称")
    private String textbookName = null;

    @ApiModelProperty("作者姓名")
    private String authorName = null;
}