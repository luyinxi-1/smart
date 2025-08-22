package com.upc.config.time;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MultiFormatLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    // 定义你希望支持的多种日期时间格式
    private static final DateTimeFormatter FULL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getText().trim();

        // 1. 尝试解析 "yyyy-MM-dd HH:mm:ss" 格式
        try {
            return LocalDateTime.parse(dateString, FULL_FORMATTER);
        } catch (DateTimeParseException e) {
            // 解析失败，继续尝试下一种格式
        }

        // 2. 尝试解析 "yyyy-MM-dd" 格式
        try {
            LocalDate date = LocalDate.parse(dateString, DATE_ONLY_FORMATTER);
            // 如果只有日期，则将其转换为当天的开始时间 (00:00:00)
            return date.atStartOfDay();
        } catch (DateTimeParseException e) {
            // 如果所有格式都失败了，可以抛出异常
            throw new IOException("无法将值 '" + dateString + "' 解析为有效的日期时间格式", e);
        }
    }
}
