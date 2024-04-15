package com.huangsam.springdemo.hello

import com.huangsam.springdemo.Routes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForObject
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloControllerLiveTest @Autowired constructor(
    private val restTemplate: TestRestTemplate,
    @LocalServerPort private val port: Int
) {
    private val baseUrl: String by lazy { "http://localhost:$port${Routes.HELLO}" }

    @Test
    fun helloNameRendersImplicit() {
        val result = restTemplate.getForObject<String>(getHelloNameUrl())
        assertEquals("Hello world $DEFAULT_SAM!", result)
    }

    @Test
    fun helloNameRendersExplicit() {
        val result = restTemplate.getForObject<String>(getHelloNameUrl("Bob"))
        assertEquals("Hello world Bob!", result)
    }

    private fun getHelloNameUrl(name: String? = null): String {
        return name?.let { "$baseUrl?name=$name" } ?: baseUrl
    }
}
