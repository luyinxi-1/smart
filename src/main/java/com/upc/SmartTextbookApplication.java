package com.upc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.upc.mapper")
@SpringBootApplication
public class SmartTextbookApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTextbookApplication.class, args);
    }

}
