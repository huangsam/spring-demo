package com.huangsam.springdemo

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SpringDemoApplicationTests {
	@Autowired
    private lateinit var helloController: HelloController

	@Test
	fun contextLoads() {
		assertNotNull(helloController)
	}
}
