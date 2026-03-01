package com.huangsam.springdemo.hello

import com.huangsam.springdemo.Routes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class HelloControllerLiveTest @Autowired constructor(
    private val restClient: RestTestClient
) {

    @Test
    fun helloNameRendersImplicit() {
        val result = restClient.get().uri(Routes.HELLO)
            .exchange()
            .expectBody(String::class.java).returnResult().responseBody
        assertEquals("Hello world ${HelloController.DEFAULT_SAM}!", result)
    }

    @Test
    fun helloNameRendersExplicit() {
        val result = restClient.get().uri("${Routes.HELLO}?name=Bob")
            .exchange()
            .expectBody(String::class.java).returnResult().responseBody
        assertEquals("Hello world Bob!", result)
    }
}
