package com.upc.modular.dataStatistics.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.upc.modular.dataStatistics.controller.param.OfflineReadingLogParam;
import com.upc.modular.dataStatistics.controller.param.SyncReceiptDTO;
import com.upc.modular.dataStatistics.entity.StudentReadingLog;
import com.upc.modular.dataStatistics.mapper.StudentReadingLogMapper;
import com.upc.modular.dataStatistics.service.IStudentReadingLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author la
 * @since 2025-07-12
 */
@Slf4j
@Service
public class StudentReadingLogServiceImpl extends ServiceImpl<StudentReadingLogMapper, StudentReadingLog> implements IStudentReadingLogService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SyncReceiptDTO saveAndReceipt(List<OfflineReadingLogParam> dtoList) {

        int total = dtoList == null ? 0 : dtoList.size();
        if (total == 0) {
            SyncReceiptDTO empty = new SyncReceiptDTO();
            empty.setSummary("上传数据为空，0 条成功，0 条失败");
            empty.setInserted(Collections.emptyList());
            empty.setSkipped(Collections.emptyList());
            return empty;
        }

        /* === 1. 提取客户端 uuid === */
        Set<String> uuids = dtoList.stream()
                .map(OfflineReadingLogParam::getClientUuid)
                .collect(Collectors.toSet());

        /* === 2. 查询已在云端存在的 uuid === */
        List<String> existed = this.list(
                        Wrappers.<StudentReadingLog>lambdaQuery()
                                .in(StudentReadingLog::getClientUuid, uuids)
                                .select(StudentReadingLog::getClientUuid)
                ).stream()
                .map(StudentReadingLog::getClientUuid)
                .collect(Collectors.toList());

        /* === 3. 生成待插入实体 === */
        List<StudentReadingLog> toInsert = dtoList.stream()
                .filter(dto -> !existed.contains(dto.getClientUuid()))
                .map(dto -> new StudentReadingLog()
                        .setStudentId(dto.getStudentId())
                        .setTextbookId(dto.getTextbookId())
                        .setTextbookCatalogId(dto.getTextbookCatalogId())
                        .setStartTime(dto.getStartTime())
                        .setDurationMinutes(dto.getDurationMinutes())
                        .setClientUuid(dto.getClientUuid())
                        .setCreator(dto.getStudentId())
                        .setAddDatetime(dto.getAddDatetime() == null
                                ? LocalDateTime.now() : dto.getAddDatetime())
                ).collect(Collectors.toList());

        /* === 4. 批量写入 === */
        boolean ok = true;
        if (!toInsert.isEmpty()) {
            ok = this.saveBatch(toInsert, 100);   // 100 条一批
        }

        int inserted = ok ? toInsert.size() : 0;
        int skipped = existed.size();
        int failed = total - inserted - skipped;   // 理论应为 0

        /* === 5. 组装回执（使用 setter） === */
        SyncReceiptDTO receipt = new SyncReceiptDTO();
        receipt.setInserted(
                toInsert.stream()
                        .map(StudentReadingLog::getClientUuid)
                        .collect(Collectors.toList())
        );
        receipt.setSkipped(existed);
        receipt.setSummary(String.format(
                "共上传 %d 条待同步数据，成功同步 %d 条（新增 %d、跳过 %d），%d 条同步失败",
                total, inserted + skipped, inserted, skipped, failed
        ));

        return receipt;
    }
}
