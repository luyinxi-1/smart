package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.textbook.entity.LearningLog;
import com.upc.modular.textbook.mapper.LearningLogMapper;
import com.upc.modular.textbook.param.RecentStudyReturnParam;
import com.upc.modular.textbook.param.UuidParam;
import com.upc.modular.textbook.service.ILearningLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-14
 */
@Service
public class LearningLogServiceImpl extends ServiceImpl<LearningLogMapper, LearningLog> implements ILearningLogService {

    private static final Logger logger = LoggerFactory.getLogger(LearningLogServiceImpl.class);

    @Autowired
    private LearningLogMapper learningLogMapper;

    @Override
    public Boolean insert(LearningLog learningLog) {
        if (ObjectUtils.isEmpty(learningLog) || ObjectUtils.isEmpty(learningLog.getTextbookId()) || ObjectUtils.isEmpty(learningLog.getCatalogueId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }

        // 默认设置为未同步状态
        learningLog.setSyncStatus(0);

        return this.save(learningLog);
    }

    @Override
    public List<RecentStudyReturnParam> recentStudy(Integer limit) {
        if (ObjectUtils.isEmpty(UserUtils.get()) || ObjectUtils.isEmpty(UserUtils.get().getId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户未登录");
        }
        Long userId = UserUtils.get().getId();

        try {
            // 获取最近学习记录
            List<RecentStudyReturnParam> recentStudies = learningLogMapper.recentStudy(userId, limit);
            logger.info("获取到最近学习记录数量: {}", recentStudies.size());

            if (recentStudies.isEmpty()) {
                logger.info("没有找到学习记录");
                return recentStudies;
            }

            // 提取教材ID列表
            List<Long> textbookIds = recentStudies.stream()
                    .map(RecentStudyReturnParam::getId)
                    .collect(Collectors.toList());
            logger.info("教材ID列表: {}", textbookIds);

            // 获取总章节数
            List<Map<String, Object>> totalChaptersList = learningLogMapper.getTextbookTotalChapters(textbookIds);
            logger.info("总章节数查询结果: {}", totalChaptersList);

            Map<Long, Integer> totalChaptersMap = totalChaptersList.stream()
                    .collect(Collectors.toMap(
                            map -> Long.valueOf(map.get("textbookId").toString()),
                            map -> Integer.valueOf(map.get("totalChapters").toString())
                    ));
            logger.info("总章节数映射: {}", totalChaptersMap);

            // 获取已读章节数
            List<Map<String, Object>> readChaptersList = learningLogMapper.getStudentReadChapters(userId, textbookIds);
            logger.info("已读章节数查询结果: {}", readChaptersList);

            Map<Long, Integer> readChaptersMap = readChaptersList.stream()
                    .collect(Collectors.toMap(
                            map -> Long.valueOf(map.get("textbookId").toString()),
                            map -> Integer.valueOf(map.get("readChapters").toString())
                    ));
            logger.info("已读章节数映射: {}", readChaptersMap);

            // 计算学习进度
            for (RecentStudyReturnParam study : recentStudies) {
                Long textbookId = study.getId();
                Integer totalChapters = totalChaptersMap.getOrDefault(textbookId, 0);
                Integer readChapters = readChaptersMap.getOrDefault(textbookId, 0);

                logger.info("教材ID: {}, 总章节数: {}, 已读章节数: {}", textbookId, totalChapters, readChapters);

                if (totalChapters > 0) {
                    double progress = (double) readChapters / totalChapters * 100;
                    int progressInt = (int) Math.round(progress);
                    study.setLearningProgress(progressInt);
                    logger.info("计算的学习进度: {}%", progressInt);
                } else {
                    study.setLearningProgress(0);
                    logger.info("总章节数为0，设置学习进度为0");
                }

                // 确保进度在 0-100 范围内
                if (study.getLearningProgress() < 0) {
                    study.setLearningProgress(0);
                } else if (study.getLearningProgress() > 100) {
                    study.setLearningProgress(100);
                }

                logger.info("最终学习进度: {}%", study.getLearningProgress());
            }

            return recentStudies;

        } catch (Exception e) {
            logger.error("计算学习进度时发生错误", e);
            // 如果计算失败，至少返回基本的学习记录，进度设为0
            List<RecentStudyReturnParam> recentStudies = learningLogMapper.recentStudy(userId, limit);
            for (RecentStudyReturnParam study : recentStudies) {
                study.setLearningProgress(0);
            }
            return recentStudies;
        }
    }

    @Override
    public Boolean batchDeleteByUuid(UuidParam uuidParam) {
        // 1. 参数校验
        if (ObjectUtils.isEmpty(uuidParam) || ObjectUtils.isEmpty(uuidParam.getUuidList())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参UUID列表不能为空");
        }

        // 2. 构建查询条件 (QueryWrapper)
        // 使用 LambdaQueryWrapper 可以防止硬编码字段名，更安全
        LambdaQueryWrapper<LearningLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(LearningLog::getClientUuid, uuidParam.getUuidList());

        // 3. 执行删除操作
        // this.remove(wrapper) 方法会根据构造的条件执行 DELETE FROM table WHERE ...
        return this.remove(wrapper);
    }

    @Override
    public List<Long> getNewLogIdsForClient() {
        LambdaQueryWrapper<LearningLog> queryWrapper = new LambdaQueryWrapper<>();
        // 只查询ID，更高效
        queryWrapper.select(LearningLog::getId).eq(LearningLog::getSyncStatus, 0);
        List<LearningLog> logs = this.list(queryWrapper);
        // 提取ID并返回
        return logs.stream().map(LearningLog::getId).collect(Collectors.toList());
    }

    @Override
    public List<LearningLog> getLogsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return this.listByIds(ids);
    }

    @Override
    @Transactional // 建议加上事务，保证这批ID的状态更新是原子性的
    public boolean confirmLogsSync(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true; // 没有ID需要更新，也算成功
        }
        LambdaUpdateWrapper<LearningLog> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(LearningLog::getId, ids) // 只更新传入的ID
                .eq(LearningLog::getSyncStatus, 0) // 增加一个条件，防止重复更新
                .set(LearningLog::getSyncStatus, 1);
        return this.update(updateWrapper);
    }
}