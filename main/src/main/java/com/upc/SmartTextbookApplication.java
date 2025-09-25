package com.upc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // <-- 2. 添加此注解以开启定时任务功能
@SpringBootApplication
public class SmartTextbookApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartTextbookApplication.class, args);
    }
}
