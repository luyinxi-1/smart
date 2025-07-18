package com.upc.modular.datastatistics.service;

import com.upc.modular.datastatistics.controller.param.TextbookNumberReturnParam;
import com.upc.modular.datastatistics.controller.param.TextbookNumberSearchParam;
import java.util.List;
public interface ITextbookStatisticsService {
    Long countTextbookNumber();

    List<TextbookNumberReturnParam> countTextbookNumberByTime(TextbookNumberSearchParam param);
}
