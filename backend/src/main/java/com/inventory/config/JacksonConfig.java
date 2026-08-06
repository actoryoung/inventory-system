package com.inventory.config;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 全局配置
 *
 * - 序列化 LocalDateTime 为 ISO 格式（前端 new Date() 可直接解析）
 * - 反序列化兼容前端 `yyyy-MM-dd HH:mm:ss` 与 ISO 格式
 *
 * @author inventory-system
 * @since 2026-08-06
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
                .serializerByType(LocalDateTime.class,
                        new LocalDateTimeSerializer(ISO_DATE_TIME))
                .deserializerByType(LocalDateTime.class,
                        new FlexibleLocalDateTimeDeserializer());
    }
}
