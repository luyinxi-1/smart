package com.upc.modular.textbook.service;

import com.upc.modular.textbook.entity.TextbookClassification;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.textbook.param.TextbookClassificationSearchParam;
import com.upc.modular.textbook.param.TopLevelTextbookClassificationSearchParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-08-12
 */
public interface ITextbookClassificationService extends IService<TextbookClassification> {

    void insertTextbookClassification(TextbookClassification param);

    void removeTextbookClassification(List<Long> idList);

    boolean updateTextbookClassification(TextbookClassification param);

    List<TextbookClassification> selectTextbookClassificationParentIdList(Integer classificationGrade);

    List<TextbookClassification> selectTextbookClassificationDownList(Long id);

    List<TextbookClassification> selectTextbookClassificationList(TextbookClassificationSearchParam param);

    List<TextbookClassification> buildDictTree(List<TextbookClassification> list);

    boolean updateTextbookClassificationSortName(Long id, Integer param);

    List<TextbookClassification> selectTopLevelTextbookClassification(TopLevelTextbookClassificationSearchParam param);
}
