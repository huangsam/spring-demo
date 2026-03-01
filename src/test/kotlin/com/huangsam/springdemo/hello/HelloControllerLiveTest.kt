package com.huangsam.springdemo.hello

import com.huangsam.springdemo.Routes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.client.RestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloControllerLiveTest @Autowired constructor(
    @param:LocalServerPort port: Int
) {
    private val restClient: RestClient = RestClient.builder().baseUrl("http://localhost:${port}").build()

    @Test
    fun helloNameRendersImplicit() {
        val result = restClient.get().uri(Routes.HELLO).retrieve().body(String::class.java)
        assertEquals("Hello world ${HelloController.DEFAULT_SAM}!", result)
    }

    @Test
    fun helloNameRendersExplicit() {
        val result = restClient.get().uri("${Routes.HELLO}?name=Bob").retrieve().body(String::class.java)
        assertEquals("Hello world Bob!", result)
    }
}
