package com.huangsam.springdemo

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/hello")
class HelloController {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    fun helloName(@RequestParam(value = "name", defaultValue = "Sam") name: String): String {
        logger.debug("We entered the helloName endpoint")
        return "Hello world $name!"
    }
}
