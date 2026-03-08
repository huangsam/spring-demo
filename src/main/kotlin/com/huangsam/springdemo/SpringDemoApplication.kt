package com.huangsam.springdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling @SpringBootApplication class SpringDemoApplication

fun main(args: Array<String>) {
    runApplication<SpringDemoApplication>(*args)
}
