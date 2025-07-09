package com.upc.modular.auth.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GetUserIsInInstitutionParam {
    @ApiModelProperty("机构id")
    private Long institutionId;

    @ApiModelProperty("教师id")
    private Long userId;
}
