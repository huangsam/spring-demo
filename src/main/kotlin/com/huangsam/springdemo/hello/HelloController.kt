package com.huangsam.springdemo.hello

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

const val HELLO_URL = "/hello"

const val DEFAULT_SAM = "Sam"

@RestController
@RequestMapping(HELLO_URL)
class HelloController(private val helloService: HelloService) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    fun helloName(
        @RequestParam(value = "name", defaultValue = DEFAULT_SAM) name: String
    ): String {
        logger.debug("We entered the helloName endpoint")
        return helloService.greet(name)
    }
}
