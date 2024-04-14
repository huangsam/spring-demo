package com.huangsam.springdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringDemoApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<SpringDemoApplication>(*args)
}
