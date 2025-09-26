package com.upc.modular.textbook.service;

import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.textbook.param.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-08
 */
public interface ITextbookCatalogService extends IService<TextbookCatalog> {

    void processAndSaveHtml(MultipartFile file, Long textbookId);

    Boolean insert(List<TextbookCatalogInsertParam> params);

    Boolean delete(IdParam idParam);

    Boolean updateTextbook(List<TextbookCatalog> param);

    void exportTextbook(HttpServletResponse response, Long textbookId);

    void exportTextbookByString(HttpServletResponse response, String html);

    List<ReadTextbookReturnParam> readTextbook(Long id);

    List<TextbookTree> getTextbookCatalogTree(Long textbookId);

    List<TextbookCatalog> downloadTextbookCatalog(Long textbookId);

    List<ReadTextbookReturnParam> readTextbookCatalog(Long textbookId, Long catalogId);
}
