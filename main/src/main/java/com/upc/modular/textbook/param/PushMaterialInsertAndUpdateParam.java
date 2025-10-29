package com.upc.modular.textbook.param;

import com.upc.modular.textbook.entity.MaterialList;
import com.upc.modular.textbook.entity.MaterialPush;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
@Data
public class PushMaterialInsertAndUpdateParam extends MaterialPush {
    @ApiModelProperty("资料列表")
    private List<MaterialList> materialList;
}
