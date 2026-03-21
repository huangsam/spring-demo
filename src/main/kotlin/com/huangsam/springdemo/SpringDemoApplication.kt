package com.huangsam.springdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling @SpringBootApplication public class SpringDemoApplication

public fun main(args: Array<String>) {
    runApplication<SpringDemoApplication>(*args)
}
