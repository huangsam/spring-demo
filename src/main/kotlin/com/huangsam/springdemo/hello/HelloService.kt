package com.huangsam.springdemo.hello

import org.springframework.stereotype.Service

@Service
class HelloService {
    fun greet(name: String): String {
        return "Hello world $name!"
    }
}
