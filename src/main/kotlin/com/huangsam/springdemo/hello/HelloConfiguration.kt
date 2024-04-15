package com.huangsam.springdemo.hello

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HelloConfiguration {
    @Bean
    fun helloService(): HelloService {
        return HelloService()
    }
}
