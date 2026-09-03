package com.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Exposes static folders so Thymeleaf can resolve style.css paths cleanly
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
    }
}
