package com.upc.modular.auth.param;

import com.upc.modular.auth.entity.SysAuthority;
import lombok.Data;

import java.util.List;

/**
 * @Author: xth
 * @Date: 2025/7/5 15:43
 */
@Data
public class SysAuthorityTreeReturnParam extends SysAuthority {

    private List<SysAuthorityTreeReturnParam> sysAuthorityList;
}
