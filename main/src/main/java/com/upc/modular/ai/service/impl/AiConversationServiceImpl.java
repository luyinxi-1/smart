package com.upc.modular.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.common.utils.UserUtils;
import com.upc.modular.ai.entity.AiConversation;
import com.upc.modular.ai.entity.AiConversationRecords;
import com.upc.modular.ai.mapper.AiConversationMapper;
import com.upc.modular.ai.mapper.AiConversationRecordsMapper;
import com.upc.modular.ai.param.AIConRecordsSessionId;
import com.upc.modular.ai.param.AiConversationOtherMonth;
import com.upc.modular.ai.param.AiConversationTitle;
import com.upc.modular.ai.param.AiConversationTitleByTime;
import com.upc.modular.ai.service.IAiConversationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-09
 */
@Service
public class AiConversationServiceImpl extends ServiceImpl<AiConversationMapper, AiConversation> implements IAiConversationService {

    @Autowired
    private AiConversationMapper aiConversationMapper;

    @Autowired
    private AiConversationRecordsMapper aiConversationRecordsMapper;

    @Override
    public Long createConversationIfNeeded(AIConRecordsSessionId param) {
        AiConversation conversation = new AiConversation();
        conversation.setTitle(param.getContent());
        conversation.setUserId(UserUtils.get().getId());
        conversation.setConversationId(param.getSessionId());
        aiConversationMapper.insert(conversation);
        return conversation.getId();
    }

    @Override
    public Boolean deleteChildTableRecord(AiConversation param) {
        Long mainTableId = param.getId();
        LambdaQueryWrapper<AiConversationRecords> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiConversationRecords::getAiConversationId,mainTableId);
        int result = aiConversationRecordsMapper.delete(queryWrapper);
        aiConversationMapper.deleteById(param.getId());
        return result>0;
    }

    @Override
    public Integer updateMainTableTitle(AiConversation param) {
        AiConversation deepseekConversations = new AiConversation();
        deepseekConversations.setTitle(param.getTitle());
        deepseekConversations.setId(param.getId());
        return aiConversationMapper.updateById(deepseekConversations);
    }

    @Override
    public AiConversationTitleByTime selectConversionTitleByTime() {
        // 获取当前用户 id
        Long userId = UserUtils.get().getId();

        // 查询该用户所有的对话记录
        LambdaQueryWrapper<AiConversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiConversation::getUserId, userId);
        List<AiConversation> list = aiConversationMapper.selectList(queryWrapper);

        // 初始化返回结果对象
        AiConversationTitleByTime dto = new AiConversationTitleByTime();
        dto.setToday(new ArrayList<>());
        dto.setYesterday(new ArrayList<>());
        dto.setLast7Days(new ArrayList<>());
        dto.setLast30Days(new ArrayList<>());

        // 暂时使用 Map 分组存储 30 天以外数据
        Map<String, List<AiConversation>> otherMonthMap = new HashMap<>();

        // 定义时间边界
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        // 今天开始时间
        LocalDateTime startOfToday = today.atStartOfDay();
        // 昨天开始时间
        LocalDateTime startOfYesterday = today.minusDays(1).atStartOfDay();
        // 7天前的开始时间
        LocalDateTime startOf7Days = today.minusDays(7).atStartOfDay();
        // 30天前的开始时间
        LocalDateTime startOf30Days = today.minusDays(30).atStartOfDay();

        // 创建分组
        List<AiConversation> todayList = new ArrayList<>();
        List<AiConversation> yesterdayList = new ArrayList<>();
        List<AiConversation> last7DaysList = new ArrayList<>();
        List<AiConversation> last30DaysList = new ArrayList<>();

        // 遍历记录，根据修改时间分组
        for (AiConversation conversation : list) {
            LocalDateTime operationDatetime = conversation.getOperationDatetime();
            if (operationDatetime.isAfter(startOfToday)) {
                // 今天
                todayList.add(conversation);
            } else if (operationDatetime.isAfter(startOfYesterday) && operationDatetime.isBefore(startOfToday)) {
                // 昨天
                yesterdayList.add(conversation);
            } else if (operationDatetime.isAfter(startOf7Days) && operationDatetime.isBefore(startOfYesterday)) {
                // 7天内（不包含今天和昨天）
                last7DaysList.add(conversation);
            } else if (operationDatetime.isAfter(startOf30Days) && operationDatetime.isBefore(startOf7Days)) {
                // 30天内（不包含7天内的）
                last30DaysList.add(conversation);
            } else {
                /// 30天以外：按月份分组，月份格式为 yyyy-MM
                String month = operationDatetime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                otherMonthMap.computeIfAbsent(month, k -> new ArrayList<>()).add(conversation);
            }
        }

        // 对各个时间段内的数据按修改时间倒序排序
        Comparator<AiConversation> timeComparator = Comparator.comparing(
                AiConversation::getOperationDatetime).reversed();

        // 对每个列表应用排序
        todayList.sort(timeComparator);
        yesterdayList.sort(timeComparator);
        last7DaysList.sort(timeComparator);
        last30DaysList.sort(timeComparator);

        // 将原始实体列表转换为 DTO 列表
        dto.setToday(convertToTitleList(todayList));
        dto.setYesterday(convertToTitleList(yesterdayList));
        dto.setLast7Days(convertToTitleList(last7DaysList));
        dto.setLast30Days(convertToTitleList(last30DaysList));

        // 将 otherMonthMap 转换为 List<DeepSeekConversionOtherMonth>，并对每个月内的数据进行排序
        List<AiConversationOtherMonth> otherMonthList = new ArrayList<>();
        List<String> sortedMonths = new ArrayList<>(otherMonthMap.keySet());
        sortedMonths.sort(Comparator.reverseOrder());

        for (String month : sortedMonths) {
            List<AiConversation> monthData = otherMonthMap.get(month);
            // 对当前月份的数据按修改时间倒序排序
            monthData.sort(timeComparator);
            AiConversationOtherMonth deepSeekConversionOtherMonth = new AiConversationOtherMonth()
                    .setDate(month)
                    .setList(convertToTitleList(monthData));
            otherMonthList.add(deepSeekConversionOtherMonth);
        }

        dto.setOtherMonths(otherMonthList);
        return dto;
    }

    private List<AiConversationTitle> convertToTitleList(List<AiConversation> conversationsList) {
        List<AiConversationTitle> titleList = new ArrayList<>(conversationsList.size());
        for (AiConversation conversation : conversationsList) {
            AiConversationTitle titleDTO = new AiConversationTitle()
                    .setId(conversation.getId())
                    .setTitle(conversation.getTitle())
                    .setConversationId(conversation.getConversationId());
            titleList.add(titleDTO);
        }
        return titleList;
    }
}
