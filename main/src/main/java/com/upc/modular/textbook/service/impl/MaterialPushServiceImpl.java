package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.textbook.entity.MaterialList;
import com.upc.modular.textbook.entity.MaterialPush;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.mapper.MaterialListMapper;
import com.upc.modular.textbook.mapper.MaterialPushMapper;
import com.upc.modular.textbook.param.MaterialPushPageSearchParam;
import com.upc.modular.textbook.param.PushMaterialBatchUpdateCatalogParam;
import com.upc.modular.textbook.param.PushMaterialInsertAndUpdateParam;
import com.upc.modular.textbook.service.IMaterialPushService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.service.ITextbookCatalogService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author JM
 * @since 2025-10-29
 */
@Service
public class MaterialPushServiceImpl extends ServiceImpl<MaterialPushMapper, MaterialPush> implements IMaterialPushService {
    @Autowired
    private MaterialListMapper materialListMapper;
    
    @Autowired
    private ITextbookCatalogService textbookCatalogService;


    @Autowired
    private ISysUserService sysUserService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertPushMaterial(PushMaterialInsertAndUpdateParam param) {
        //插入MaterialPush表内容
        MaterialPush materialPush = new MaterialPush();
        BeanUtils.copyProperties(param, materialPush);

        this.save(materialPush);
        Long materialPushId = materialPush.getId();

        //清空Material表中数据
        materialListMapper.delete(new LambdaQueryWrapper<MaterialList>()
                .eq(MaterialList::getMaterialPushId , materialPushId));
        //插入MaterialList表内容
        List<MaterialList> materialList = param.getMaterialList();
        if(materialList != null && !materialList.isEmpty()){
            for(MaterialList material : materialList){
                material.setId(null);
                material.setMaterialPushId(materialPushId); // 设置外键关联
                materialListMapper.insert(material);
            }
        }
        return materialPushId;
    }

    @Override
    public void deleteIdeologicalMaterialByIds(List<Long> ids) {
        this.removeBatchByIds(ids);
        materialListMapper.delete(new LambdaQueryWrapper<MaterialList>()
                .in(MaterialList::getMaterialPushId , ids));
    }

    @Override
    public PushMaterialInsertAndUpdateParam getMaterialById(Long id) {
        List<MaterialList> materialList = materialListMapper.selectList(new LambdaQueryWrapper<MaterialList>()
                .eq(MaterialList::getMaterialPushId , id));
        MaterialPush materialPush = this.getById(id);
        PushMaterialInsertAndUpdateParam param = new PushMaterialInsertAndUpdateParam();
        param.setMaterialList(materialList);
        BeanUtils.copyProperties(materialPush, param);
        return param;
    }

    @Override
    public PageBaseReturnParam<MaterialPush> getPushMaterialByTextbookIdPage(MaterialPushPageSearchParam param) {
        Page<MaterialPush> page = new Page<>(param.getCurrent(), param.getSize());
        LambdaQueryWrapper<MaterialPush> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MaterialPush::getTextbookId, param.getTextbookId());

        // 添加章节ID查询条件
        if (param.getTextbookCatalogId() != null) {
            queryWrapper.eq(MaterialPush::getTextbookCatalogId, param.getTextbookCatalogId());
        }

        // 添加名称模糊查询条件
        if (StringUtils.hasText(param.getName())) {
            queryWrapper.like(MaterialPush::getName, param.getName());
        }

        Page<MaterialPush> result = this.page(page, queryWrapper);

  /*      List<MaterialPush> records = result.getRecords();
        records.forEach(materialPush -> {
            materialPush.setContent(null);
            materialPush.setIntroduction(null);
        });*/
        List<MaterialPush> records = result.getRecords();
        for (MaterialPush materialPush : records) {
            materialPush.setContent(null);
            materialPush.setIntroduction(null);

            // 设置创建者昵称
            if (materialPush.getCreator() != null) {
                SysTbuser user = sysUserService.getById(materialPush.getCreator());
                if (user != null) {
                    materialPush.setNickname(user.getNickname());
                }
            }

            // 计算文件数量
            LambdaQueryWrapper<MaterialList> fileListWrapper = new LambdaQueryWrapper<>();
            fileListWrapper.eq(MaterialList::getMaterialPushId, materialPush.getId());
            int fileCount = materialListMapper.selectCount(fileListWrapper).intValue();
            materialPush.setFileCount(fileCount);
        }
        result.setRecords(records);

        return PageBaseReturnParam.ok(result);

    }

    @Override
    public void updatePushMaterialById(PushMaterialInsertAndUpdateParam param) {
        // 更新material_push表数据
        MaterialPush materialPush = new MaterialPush();
        BeanUtils.copyProperties(param, materialPush);
        this.updateById(materialPush);

        // 删除material_list表中与该推送关联的所有内容
        materialListMapper.delete(new LambdaQueryWrapper<MaterialList>()
                .eq(MaterialList::getMaterialPushId, param.getId()));

        // 插入新的material_list数据
        List<MaterialList> materialList = param.getMaterialList();
        if (materialList != null && !materialList.isEmpty()) {
            for (MaterialList item : materialList) {
                item.setId(null); // 确保ID为null，让数据库自动生成
                materialListMapper.insert(item);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateCatalog(List<PushMaterialBatchUpdateCatalogParam> params) {
        // 1. 基本参数校验
        if (CollectionUtils.isEmpty(params)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "请求参数列表不能为空");
        }

        // 2. 遍历参数列表进行逐个更新
        for (PushMaterialBatchUpdateCatalogParam param : params) {
            if (param.getId() == null) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "资料推送ID不能为空");
            }

            // 校验章节ID和临时UUID至少要有一个
            if (param.getTextbookCatalogId() == null && !StringUtils.hasText(param.getTextbookCatalogUuid())) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                        "更新ID为 " + param.getId() + " 的资料时，章节ID和章节UUID必须至少提供一个");
            }

            Long finalCatalogId;
            String finalCatalogName;

            // 优先使用章节ID
            if (param.getTextbookCatalogId() != null) {
                finalCatalogId = param.getTextbookCatalogId();
                finalCatalogName = param.getTextbookCatalogName();
            } else {
                // 如果章节ID为空，则使用临时UUID去数据库查询对应的ID
                TextbookCatalog textbookCatalog = textbookCatalogService.getOne(new LambdaQueryWrapper<TextbookCatalog>()
                        .eq(TextbookCatalog::getCatalogUuid, param.getTextbookCatalogUuid()));

                // 如果根据UUID没有找到对应的章节，则抛出异常
                if (textbookCatalog == null) {
                    throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                            "根据提供的UUID: " + param.getTextbookCatalogUuid() + " 未找到对应的章节");
                }
                finalCatalogId = textbookCatalog.getId();
                finalCatalogName = textbookCatalog.getCatalogName();
            }

            // 3. 执行单条更新
            MaterialPush updateEntity = new MaterialPush();
            updateEntity.setId(param.getId());
            updateEntity.setTextbookCatalogId(finalCatalogId);
            updateEntity.setTextbookCatalogName(finalCatalogName);

            // 使用 updateById 方法进行更新，更为精确和高效
            this.updateById(updateEntity);
        }
    }
        @Override
        public List<MaterialPush> listByTextbookId(Long textbookId) {
            return this.list(new LambdaQueryWrapper<MaterialPush>()
                    .eq(MaterialPush::getTextbookId, textbookId));
        }
        @Override
        public List<MaterialList> listMaterialListByTextbookId(Long textbookId) {
            // material_list 表本身没有 textbook_id，用 material_push 做一次 join
            return materialListMapper.selectByTextbookId(textbookId);
        }

}