package com.huangsam.springdemo

import org.springframework.stereotype.Service

@Service
class HelloService {
    fun greet(name: String): String {
        return "Hello world $name!"
    }
}
