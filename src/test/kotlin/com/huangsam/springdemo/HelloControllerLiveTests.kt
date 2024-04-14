package com.huangsam.springdemo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForObject
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloControllerLiveTests {
    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @LocalServerPort
    private var port: Int = 0

    @Test
    fun helloNameRendersImplicit() {
        val result = restTemplate.getForObject<String>(getHelloNameUrl())
        assertEquals("Hello world Sam!", result)
    }

    @Test
    fun helloNameRendersExplicit() {
        val result = restTemplate.getForObject<String>(getHelloNameUrl("Bob"))
        assertEquals("Hello world Bob!", result)
    }

    private fun getHelloNameUrl(name: String? = null): String {
        return name?.let { "http://localhost:$port/hello?name=$name" }
            ?: "http://localhost:$port/hello"
    }
}
