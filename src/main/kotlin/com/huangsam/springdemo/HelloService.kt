package com.huangsam.springdemo

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class HelloService {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun greet(name: String): String {
        logger.debug("We entered the greet method")
        return "Hello world $name!"
    }
}
