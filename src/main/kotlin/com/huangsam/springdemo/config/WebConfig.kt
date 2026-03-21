package com.huangsam.springdemo.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
public class WebConfig(private val rateLimitInterceptor: RateLimitInterceptor) : WebMvcConfigurer {

    public override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**", "/hello/**")
    }
}
