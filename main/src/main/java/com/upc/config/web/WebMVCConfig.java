package com.upc.config.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
@Slf4j
public class WebMVCConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //swagger增强的静态资源映射
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        //flowable设计器静态资源映射
        registry.addResourceHandler("/designer/**").addResourceLocations("classpath:/designer/");
        File files = new File("upload");
        String path = files.getAbsolutePath();
        registry.addResourceHandler("/upload/**").addResourceLocations("file:" + path + "/");
    }

}
