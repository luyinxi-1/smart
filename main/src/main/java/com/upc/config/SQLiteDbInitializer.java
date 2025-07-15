package com.upc.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import java.io.*;
import java.nio.file.*;

public class SQLiteDbInitializer implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        try {
            ConfigurableEnvironment env = event.getEnvironment();

            // 目标路径：用户目录下的 .SmartTextbook/client.db
            Path targetPath = Paths.get(System.getProperty("user.home"), ".SmartTextbook", "client.db");

            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath.getParent());

                try (InputStream is = getClass().getResourceAsStream("/client.db")) {
                    Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("SQLite数据库文件 已复制到：" + targetPath);
                }
            }

            // 设置系统变量供 application.yml 使用
            System.setProperty("db.path", targetPath.toAbsolutePath().toString());

        } catch (IOException e) {
            throw new RuntimeException("无法初始化 SQLite 数据库文件", e);
        }
    }
}