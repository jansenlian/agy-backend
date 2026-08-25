package com.greatmap.agy.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Jackson 全局序列化配置
 * 使用 extendMessageConverters 增强 Spring 默认注册的 MappingJackson2HttpMessageConverter：
 * 1. 自动将所有 Long / long 类型转为 String 输出，防止前端 19 位 JavaScript 精度丢失
 * 2. LocalDateTime 统一输出与反序列化为 yyyy-MM-dd HH:mm:ss 格式
 * 3. 完美保留 SpringDoc OpenAPI 原生文档消息转换器，杜绝 Swagger UI 页面空白问题
 */
@Configuration
public class JacksonConfig implements WebMvcConfigurer {

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                ObjectMapper objectMapper = jacksonConverter.getObjectMapper();

                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                SimpleModule simpleModule = new SimpleModule();
                simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
                simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
                objectMapper.registerModule(simpleModule);

                JavaTimeModule javaTimeModule = new JavaTimeModule();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
                javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
                javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
                objectMapper.registerModule(javaTimeModule);
            }
        }
    }
}
