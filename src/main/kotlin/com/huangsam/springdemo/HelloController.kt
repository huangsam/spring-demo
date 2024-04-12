package com.huangsam.springdemo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/hello")
class HelloController {
    @GetMapping
    fun helloName(@RequestParam(value = "name", defaultValue = "Sam") name: String?): String {
        return String.format("Hello world %s!", name)
    }
}
