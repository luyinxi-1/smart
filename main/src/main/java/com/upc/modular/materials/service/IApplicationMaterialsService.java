package com.upc.modular.materials.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsPageParam;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsSaveParam;
import com.upc.modular.materials.controller.param.vo.ApplicationMaterialsVO;
import com.upc.modular.materials.entity.ApplicationMaterials;

import java.util.List;

/**
 * <p>
 * 应用素材服务接口
 * </p>
 *
 * @author system
 * @since 2025-10-29
 */
public interface IApplicationMaterialsService extends IService<ApplicationMaterials> {

    /**
     * 新增应用素材
     *
     * @param param 应用素材参数
     * @return 应用素材ID
     */
    Long saveApplicationMaterials(ApplicationMaterialsSaveParam param);

    /**
     * 更新应用素材
     *
     * @param param 应用素材参数
     * @return 是否成功
     */
    boolean updateApplicationMaterials(ApplicationMaterialsSaveParam param);

    /**
     * 删除应用素材
     *
     * @param id 应用素材ID
     * @return 是否成功
     */
    boolean deleteApplicationMaterials(Long id);

    /**
     * 批量删除应用素材
     *
     * @param ids 应用素材ID列表
     * @return 是否成功
     */
    boolean batchDeleteApplicationMaterials(List<Long> ids);

    /**
     * 根据ID查询应用素材详情
     *
     * @param id 应用素材ID
     * @param includeTeachingMaterials 是否包含关联的教学素材
     * @return 应用素材详情
     */
    ApplicationMaterialsVO getApplicationMaterialsById(Long id, boolean includeTeachingMaterials);

    /**
     * 分页查询应用素材列表
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<ApplicationMaterialsVO> getApplicationMaterialsPage(ApplicationMaterialsPageParam param);

    /**
     * 应用素材关联教学素材
     *
     * @param applicationMaterialId 应用素材ID
     * @param teachingMaterialIds 教学素材ID列表
     * @return 是否成功
     */
    boolean relateTeachingMaterials(Long applicationMaterialId, List<Long> teachingMaterialIds);

    /**
     * 移除应用素材关联的教学素材
     *
     * @param applicationMaterialId 应用素材ID
     * @param teachingMaterialIds 教学素材ID列表
     * @return 是否成功
     */
    boolean removeTeachingMaterials(Long applicationMaterialId, List<Long> teachingMaterialIds);
}
