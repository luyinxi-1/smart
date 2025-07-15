package com.upc.config.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.context.annotation.Import;
import springfox.documentation.spring.web.SpringfoxWebMvcConfiguration;

/**
 * Knife4j 配置类
 * 用于配置API文档信息和分组
 * 解决了 Spring Boot 2.6+ 与 Springfox 3.0.0 的兼容性问题
 *
 * 重要的兼容性设置已通过在 application.properties 中配置 'spring.mvc.pathmatch.matching-strategy=ant_path_matcher' 来实现。
 */
@Configuration
@EnableSwagger2WebMvc // 启用 Swagger 2 MVC 支持
@EnableWebMvc // 确保Spring MVC配置正常工作
@Import(SpringfoxWebMvcConfiguration.class) // 导入Springfox的WebMvc配置，确保其内部组件被正确扫描
public class Knife4jConfig implements WebMvcConfigurer { // 实现 WebMvcConfigurer 接口

    /**
     * 解决 Spring Boot 2.6.x 以上版本与 Springfox 3.0.0 的兼容性问题。
     * 此方法本身不再需要额外代码，因为主要兼容性设置已移至 application.properties。
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Spring Boot 2.6.x 以后默认使用 PathPatternParser
        // Springfox 3.0.0 与之不兼容会导致 NullPointerException
        // 最佳实践是在 application.properties 中添加：spring.mvc.pathmatch.matching-strategy=ant_path_matcher
        // 如果该配置不存在，可能会继续遇到兼容性问题。
    }

    /**
     * 配置全局 API 信息，作为所有分组的基础信息
     * @return ApiInfo 对象
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("智慧教材开发管理平台接口文档") // 文档标题
                .description("统一接口信息，方便前后端联调") // 文档描述
                .version("1.0") // 文档版本
                .contact(new Contact("upc团队", "http://www.example.com", "contact@example.com")) // 联系人信息
                .build();
    }

    /**
     * 配置默认的 API 文档分组
     * 可以包含所有接口，或者作为未明确分组接口的集合
     *
     * @return Docket 对象
     */
    @Bean
    public Docket defaultApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo()) // 应用全局API信息
                .groupName("所有接口") // 设置分组名称
                .select()
                // 指定要扫描的包路径
                // 可以根据需要调整，例如扫描整个根包 "com.upc"
                .apis(RequestHandlerSelectors.basePackage("com.upc"))
                // 过滤路径，这里表示所有路径都包含
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 分组 1：学生管理
     * 扫描 com.upc.modular.student.controller 包下的接口
     * @return Docket 对象
     */
    @Bean
    public Docket studentGroup() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("学生管理")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.upc.modular.student.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 分组 2：班级管理
     * 扫描 com.upc.modular.group.controller 包下的接口
     * @return Docket 对象
     */
    @Bean
    public Docket groupGroup() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("班级管理")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.upc.modular.group.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 分组 3：课程管理
     * 扫描 com.upc.modular.course.controller 包下的接口
     * @return Docket 对象
     */
    @Bean
    public Docket courseGroup() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("课程管理")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.upc.modular.course.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 分组 4：智慧教材
     * 扫描 com.upc.modular.textbook.controller 包下的接口
     * @return Docket 对象
     */
    @Bean
    public Docket textbookGroup() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("智慧教材")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.upc.modular.textbook.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 分组 5：用户与权限管理
     * 扫描 com.upc.modular.auth.controller 包下的接口
     * @return Docket 对象
     */
    @Bean
    public Docket authGroup() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("用户与权限管理")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.upc.modular.auth.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public Docket teacher() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("教师管理")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.upc.modular.teacher.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public Docket institution() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("机构管理")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.upc.modular.institution.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public Docket discussion() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("教学活动")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.upc.modular.teachingactivities.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public Docket questionbank() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("题库管理")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.upc.modular.questionbank.controller"))
                .paths(PathSelectors.any())
                .build();
    }
    @Bean
    public Docket textbook() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("教材管理")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.upc.modular.textbook.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public Docket dataStatistics() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("数据统计")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.upc.modular.datastatistics.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public Docket homePage() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("首页管理")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.upc.modular.homepage.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public Docket client() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("客户端")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.upc.modular.client.controller"))
                .paths(PathSelectors.any())
                .build();
    }
}
