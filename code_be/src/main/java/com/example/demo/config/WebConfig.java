package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor

public class WebConfig implements WebMvcConfigurer {
    private final MaintenanceInterceptor maintenanceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(maintenanceInterceptor)
                .addPathPatterns("/**") //tất cả URL
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/webjars/**"); //trừ tài nguyên tĩnh
    }
}