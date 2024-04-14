package com.huangsam.springdemo

import com.huangsam.springdemo.blog.HtmlController
import com.huangsam.springdemo.hello.HelloController
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SpringDemoApplicationTests(
    @Autowired private val helloController: HelloController,
    @Autowired private val htmlController: HtmlController,
) {
    @Test
    fun contextLoads() {
        assertNotNull(helloController)
        assertNotNull(htmlController)
    }
}
