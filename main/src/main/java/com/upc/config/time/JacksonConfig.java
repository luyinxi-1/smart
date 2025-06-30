package com.upc.config.time;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
            JavaTimeModule timeModule = new JavaTimeModule();
            timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dtf));
            timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dtf));
            builder.modules(timeModule);
        };
    }
}
