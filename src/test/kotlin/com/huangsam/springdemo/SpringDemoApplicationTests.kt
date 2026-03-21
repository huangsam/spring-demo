package com.huangsam.springdemo

import com.huangsam.springdemo.blog.HtmlController
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
internal class SpringDemoApplicationTests(
    @param:Autowired private val htmlController: HtmlController
) {
    @Test
    internal fun contextLoads() {
        assertNotNull(htmlController)
    }
}
