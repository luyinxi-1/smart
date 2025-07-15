package com.upc.codeGenerator;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.IFill;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.upc.constant.MybatisConst.*;

/**
 * 代码生成器配置类
 * 每次用完生成工具，应当讲主方法注释掉，
 * 不然有可能会忘记切换直接生成导致覆盖掉
 * @see <a href="https://baomidou.com/pages/981406/">代码生成器配置</a>
 *
 * @author 邢同昊
 */
public class CodeGeneratorConfig {

    private final String[] tableNames;

    private final String author;


    private final String moduleName;


    public CodeGeneratorConfig(String author, String moduleName, String... tableNames) {
        this.author = author;
        this.tableNames = tableNames;
        this.moduleName = moduleName;
    }

    public void generate() {
//        String url = "jdbc:mysql://119.186.61.60/future_village";
//        String url = "jdbc:mysql://182.254.147.39/village";
//        String url = "jdbc:mysql://27.223.88.150:10003/new_restructure";
        String url = "jdbc:kingbase8://172.19.162.132:54321/kingbase";
        String username = "system";
        String password = "123456789";

        FastAutoGenerator.create(url, username, password)
                // 全局配置
                .globalConfig(builder -> {
                    // 设置作者
                    builder.author(author)
                            // 开启 swagger 模式
                            .enableSwagger()
                            // 覆盖已生成文件
                            .fileOverride()
                            // 输出目录
                            .outputDir(System.getProperty("user.dir") + "/" + moduleName + "/src/main/java")
                            //开启Swagger注解
                            .enableSwagger()
                            // 禁止打开输出目录
                            .disableOpenDir()
                            // 将数据库中的timestamp映射为java.util.Date
                            .dateType(DateType.TIME_PACK);
                })
                // 包配置
                .packageConfig(builder -> {
                    // 设置父包名
                    builder.parent("com.upc.modular.homepage")
                            .controller("controller")
                            .entity("entity")
                            .service("service")
                            .serviceImpl("service.impl")
                            .mapper("mapper")
                            .pathInfo(Collections.singletonMap(OutputFile.mapperXml,
                                    System.getProperty("user.dir") + "/" + moduleName + "/src/main/resources/mapper/homepage"))
                    ;
                })
                //生成策略配置
                .strategyConfig(builder -> {
                    builder
                            //  生成Controller是RestController
                            .controllerBuilder()
                            .enableRestStyle();
                    // 设置需要生成的表名
                    builder.addInclude(tableNames);
                    //实体类配置策略
                    builder.entityBuilder()
                            .enableLombok()
                            .enableChainModel()
                            .enableTableFieldAnnotation()
                            .addTableFills(getFillList())
                    ;
                    builder
                            // 生成xml文件时生成BaseResultMap
                            .mapperBuilder()
                            .enableMapperAnnotation()
                            .enableBaseResultMap();

                })
//                .templateConfig(builder -> builder
////                        // 如果参数为空，则全部不覆盖
////                        // 如果有参数，则除了有参数的全部覆盖
//                        .disable(
////                                //这里没写实体类，则是只覆盖测试类
////                                //要什么就注释什么
//                                TemplateType.SERVICE,
//                                TemplateType.SERVICEIMPL,
//                                TemplateType.CONTROLLER,
//                                TemplateType.ENTITY,
//                                TemplateType.MAPPER,
//                                //实体类修改后，xml应手动自行更改对应ResultMap
//                                TemplateType.XML
//                        )
//                )
                // 使用Freemarker引擎模板
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    /**
     * 得到entity自动填充字段列表
     * @return 字段列表
     */
    private List<IFill> getFillList() {
        ArrayList<IFill> fillList = new ArrayList<>();
        fillList.add(new Column(CREATOR, FieldFill.INSERT));
        fillList.add(new Column(ADD_DATE_TIME_NAMING_THE_LINE_METHOD, FieldFill.INSERT));
        fillList.add(new Column(OPERATOR, FieldFill.UPDATE));
        fillList.add(new Column(OPERATION_DATE_TIME_NAMING_THE_LINE_METHOD, FieldFill.UPDATE));
        return fillList;
    }
}
