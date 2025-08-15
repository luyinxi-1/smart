package com.upc.modular.textbook.param;

import com.upc.modular.textbook.entity.Textbook;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserFavoritesVO {
    private Long id;                // 收藏记录 ID
    private Long userId;
    private Long textbookId;
    private String textbookName;
    private Long classification;
    private LocalDateTime addDatetime;     // 收藏时间

    // 教材详情
    private Textbook textbook;
}
