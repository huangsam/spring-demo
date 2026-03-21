package com.huangsam.springdemo.blog

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
internal class FeedControllerTest @Autowired constructor(private val restClient: RestTestClient) {

    @Test
    internal fun `Assert RSS feed works`() {
        val responseBody =
            restClient
                .get()
                .uri("/rss")
                .exchange()
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType("application/rss+xml;charset=UTF-8")
                .expectBody(String::class.java)
                .returnResult()
                .responseBody

        responseBody?.let {
            assertTrue(
                it.contains("<rss") && it.contains("version=\"2.0\""),
                "Should contain RSS version",
            )
            assertTrue(it.contains("<title>Spring Demo Blog</title>"), "Should contain blog title")
            assertTrue(it.contains("<link>http://localhost:"), "Should contain base URL")
            assertTrue(it.contains("<item>"), "Should contain at least one item")
        }
    }

    @Test
    internal fun `Assert Atom feed works`() {
        val responseBody =
            restClient
                .get()
                .uri("/atom")
                .exchange()
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType("application/atom+xml;charset=UTF-8")
                .expectBody(String::class.java)
                .returnResult()
                .responseBody

        responseBody?.let {
            assertTrue(
                it.contains("<feed xmlns=\"http://www.w3.org/2005/Atom\""),
                "Should contain Atom namespace",
            )
            assertTrue(it.contains("<title>Spring Demo Blog</title>"), "Should contain blog title")
            assertTrue(it.contains("<entry>"), "Should contain at least one entry")
        }
    }
}
