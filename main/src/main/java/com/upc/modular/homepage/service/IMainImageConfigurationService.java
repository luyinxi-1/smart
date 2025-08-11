package com.upc.modular.homepage.service;

import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.homepage.entity.MainImageConfiguration;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-08-11
 */
public interface IMainImageConfigurationService extends IService<MainImageConfiguration> {

    Boolean insert(MainImageConfiguration param);

    Boolean batchDelete(IdParam idParam);

    Boolean updateConfiguration(MainImageConfiguration param);

    List<MainImageConfiguration> selectALlConfiguration();
}
