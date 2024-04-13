package com.huangsam.springdemo

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.Assertions.*

@SpringBootTest
class SpringDemoApplicationTests {

	@Autowired
    private lateinit var controller: HelloController

	@Test
	fun contextLoads() {
		assertNotNull(controller)
	}

}
