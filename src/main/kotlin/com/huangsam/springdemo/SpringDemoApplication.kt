package com.huangsam.springdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@SpringBootApplication
@RestController
class SpringDemoApplication {
    @GetMapping("/hello")
    fun hello(@RequestParam(value = "name", defaultValue = "Sam") name: String?): String {
        return String.format("Hello world %s!", name)
    }
}

fun main(args: Array<String>) {
	runApplication<SpringDemoApplication>(*args)
}
