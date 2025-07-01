package com.upc.modular.auth.param;

import com.upc.modular.institution.entity.Institution;
import lombok.Data;

import java.util.List;

/**
 * @Author: xth
 * @Date: 2025/7/1 20:46
 */
@Data
public class InstitutionDto extends Institution {
    private List<InstitutionDto> children; // 子机构列表
}
