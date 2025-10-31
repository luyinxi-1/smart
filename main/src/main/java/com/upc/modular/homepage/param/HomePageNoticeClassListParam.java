package com.upc.modular.homepage.param;

import com.upc.modular.homepage.entity.HomePageNotice;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class HomePageNoticeClassListParam extends HomePageNotice {
    @ApiModelProperty("班级列表")
    private List<Long> classList;
}
