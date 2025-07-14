package com.upc.modular.textbook.service;

import com.upc.common.responseparam.R;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.textbook.param.TextbookCatalogDto;
import com.upc.modular.textbook.param.WordRequest;
import org.springframework.web.multipart.MultipartFile;

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
}
