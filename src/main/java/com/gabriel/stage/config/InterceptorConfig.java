package com.gabriel.stage.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author: Gabriel
 * @date: 2020/2/5 14:15
 * @description 登录拦截配置
 */
@Configuration
@Order(1)
public class InterceptorConfig implements WebMvcConfigurer, InitializingBean {
    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor).addPathPatterns("/**");
    }


    /**
     * Date格式化字符串
     */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**
     * DateTime格式化字符串
     */
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * Time格式化字符串
     */
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");


    @Autowired(required = false)
    private ObjectMapper objectMapper;


    /**
     * 实现InitializingBean接口,重写afterPropertiesSet方法用于处理返回的数据
     * 解决数据转换问题（例如BigDecimal转换为String,解决精度问题）
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (objectMapper != null) {
            SimpleModule simpleModule = getSimpleModule();
            objectMapper.registerModule(simpleModule);
            // 为mapper注册一个带有SerializerModifier的Factory，处理null值
            objectMapper.setSerializerFactory(objectMapper.getSerializerFactory()
            //CustomizeBeanSerializerModifier 自定义序列化修改器
                    .withSerializerModifier(new CustomizeBeanSerializerModifier()));
        }
    }

    private SimpleModule getSimpleModule() {
        // long 转换为字符串
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // 浮点型使用字符串
        simpleModule.addSerializer(Double.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Double.TYPE, ToStringSerializer.instance);
        simpleModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);

        // java8 时间格式化
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATETIME_FORMAT));
        simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMAT));
        simpleModule.addSerializer(LocalTime.class, new LocalTimeSerializer(TIME_FORMAT));

        return simpleModule;
    }
}