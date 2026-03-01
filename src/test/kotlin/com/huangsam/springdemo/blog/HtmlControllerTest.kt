package com.huangsam.springdemo.blog

import com.huangsam.springdemo.Routes
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class HtmlControllerTest @Autowired constructor(
    private val restClient: RestTestClient
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @BeforeAll
    fun setUp() {
        logger.info("Set up HTML test")
    }

    @AfterAll
    fun tearDown() {
        logger.info("Tear down HTML test")
    }

    @Test
    fun `Assert blog page title, content and status code`() {
        logger.info("Assert blog page works")
        val responseBody = restClient.get().uri(Routes.ROOT)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java).returnResult().responseBody
        responseBody!!.let {
            assertTrue(it.contains("<h1>Blog</h1>"))
            assertTrue(it.contains("Lorem"))
        }
    }

    @Test
    fun `Assert article page title, content and status code`() {
        logger.info("Assert article page works")
        val title = "Lorem"
        val responseBody = restClient.get().uri("${Routes.ARTICLE}/${title.toSlug()}")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java).returnResult().responseBody
        responseBody!!.let {
            assertTrue(it.contains(title))
            assertTrue(it.contains("Lorem"))
            assertTrue(it.contains("dolor sit amet"))
        }
    }

    @Test
    fun `Assert markdown rendering works`() {
        val title = "Markdown Test"
        val responseBody = restClient.get().uri("${Routes.ARTICLE}/${title.toSlug()}")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java).returnResult().responseBody
        responseBody!!.let {
            assertTrue(it.contains("<strong>bold</strong>"))
            assertTrue(it.contains("<ul>"))
            assertTrue(it.contains("<li>Item 1</li>"))
            assertTrue(it.contains("<a href=\"https://www.google.com\">Link to Google</a>"))
        }
    }
}
