package com.upc.modular.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.modular.questionbank.entity.StudentExercisesContent;
import com.upc.modular.questionbank.mapper.StudentExercisesContentMapper;
import com.upc.modular.questionbank.service.IStudentExercisesContentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
@Service
public class StudentExercisesContentServiceImpl extends ServiceImpl<StudentExercisesContentMapper, StudentExercisesContent> implements IStudentExercisesContentService {

//    @Override
//    public void inserStudentExercisesContent(StudentExercisesContent param) {
//        Long textbookId = param.getTextbookId();
//        Long textbookCatalogId = param.getTextbookCatalogId();
//
//        // 教材ID和教材目录ID是外键，需要判断是否存在
//        LambdaQueryWrapper<Textbook> queryWrapper1 = new LambdaQueryWrapper<>();
//        queryWrapper1.eq(Textbook::getId,textbookId);
//        boolean isTextbookExists = textbookMapper.exists(queryWrapper1);
//        if (!isTextbookExists) {
//            throw new RuntimeException("ID为 " + textbookId + " 的教材不存在！");
//        }
//
//        LambdaQueryWrapper<TextbookCatalog> queryWrapper2 = new LambdaQueryWrapper<>();
//        queryWrapper2.eq(TextbookCatalog::getId,textbookCatalogId);
//        boolean isTextbookCatalogExists = textbookCatalogMapper.exists(queryWrapper2);
//        if (!isTextbookCatalogExists) {
//            throw new RuntimeException("ID为 " + textbookCatalogId + " 的教材目录不存在！");
//        }
//
//        this.save(param);
//    }
}
