package com.upc;

import com.upc.config.SQLiteDbInitializer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartTextbookApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SmartTextbookApplication.class);
        app.addListeners(new SQLiteDbInitializer()); // 注册初始化监听器
        app.run(args);
    }
}
