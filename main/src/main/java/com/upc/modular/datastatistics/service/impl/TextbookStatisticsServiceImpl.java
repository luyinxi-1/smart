package com.upc.modular.datastatistics.service.impl;

import com.upc.modular.datastatistics.controller.param.TextbookNumberReturnParam;
import com.upc.modular.datastatistics.controller.param.TextbookNumberSearchParam;
import com.upc.modular.datastatistics.service.IStudentReadingLogService;
import com.upc.modular.datastatistics.service.ITextbookStatisticsService;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.mapper.TextbookMapper;
import com.upc.modular.textbook.service.ITextbookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TextbookStatisticsServiceImpl implements ITextbookStatisticsService {

    @Autowired
    private ITextbookService textbookService;

    @Override
    public Long countTextbookNumber() {
        return textbookService.count();
    }

    @Override
    public List<TextbookNumberReturnParam> countTextbookNumberByTime(TextbookNumberSearchParam param) {
        List<Textbook> list = textbookService.list();
        List<TextbookNumberReturnParam> resultList = new ArrayList<>();
        if (param.getQueryMethod().equals(1)) {
            Map<Integer, Long> collect = list
                    .stream()
                    .filter(textbook -> textbook.getTextbookPublishingTime() != null)
                    .collect(Collectors.groupingBy(
                            textbook -> textbook.getTextbookPublishingTime().getYear(),
                            Collectors.counting()
                    ));
            for (Map.Entry<Integer, Long> entry : collect.entrySet()) {
                TextbookNumberReturnParam returnParam = new TextbookNumberReturnParam();
                returnParam.setTime(String.valueOf(entry.getKey()));
                returnParam.setNumber(entry.getValue().intValue());
                resultList.add(returnParam);
            }
            return resultList;
        }
        if (param.getQueryMethod().equals(2)) {
            // 按年月统计
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

            Map<String, Long> monthCountMap = list.stream()
                    .filter(textbook -> textbook.getTextbookPublishingTime() != null)
                    .collect(Collectors.groupingBy(
                            textbook -> textbook.getTextbookPublishingTime().format(monthFormatter),
                            Collectors.counting()
                    ));

            for (Map.Entry<String, Long> entry : monthCountMap.entrySet()) {
                TextbookNumberReturnParam returnParam = new TextbookNumberReturnParam();
                returnParam.setTime(entry.getKey());
                returnParam.setNumber(entry.getValue().intValue());
                resultList.add(returnParam);
            }
            return resultList;
        }
        TextbookNumberReturnParam returnParam = new TextbookNumberReturnParam();
        returnParam.setNumber(list.size());
        resultList.add(returnParam);
        return resultList;
    }
}
