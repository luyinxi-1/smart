package com.upc.modular.textbook.param;

import com.upc.common.requestparam.PageBaseSearchParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TopLevelTextbookClassificationSearchParam extends PageBaseSearchParam {
}
