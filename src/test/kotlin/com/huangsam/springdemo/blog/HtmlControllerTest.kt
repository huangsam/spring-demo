package com.huangsam.springdemo.blog

import com.huangsam.springdemo.Routes
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class HtmlControllerTest @Autowired constructor(private val restClient: RestTestClient) {
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
        val responseBody =
            restClient
                .get()
                .uri(Routes.ROOT)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()
                .responseBody
        responseBody!!.let {
            assertTrue(it.contains("<h1>Blog</h1>"))
            assertTrue(it.contains("Lorem"))
            assertTrue(it.contains("views"))
            assertTrue(it.contains("likes"))
        }
    }

    @Test
    fun `Assert blog pagination navigation works`() {
        val responseBody =
            restClient
                .get()
                .uri("${Routes.ROOT}?page=0")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()
                .responseBody
        responseBody!!.let {
            assertTrue(it.contains("<h1>Blog</h1>"))
            assertTrue(it.contains("Page 1 of 1"))
        }
    }

    @Test
    fun `Assert article page title, content and status code`() {
        logger.info("Assert article page works")
        val title = "Lorem"
        val responseBody =
            restClient
                .get()
                .uri("${Routes.ARTICLE}/${title.toSlug()}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()
                .responseBody
        responseBody!!.let {
            assertTrue(it.contains(title))
            assertTrue(it.contains("Lorem"))
            assertTrue(it.contains("dolor sit amet"))
            assertTrue(it.contains("views"))
            assertTrue(it.contains("likes"))
            assertTrue(it.contains("Like"))
        }
    }

    @Test
    fun `Assert markdown rendering works`() {
        val title = "Markdown Test"
        val responseBody =
            restClient
                .get()
                .uri("${Routes.ARTICLE}/${title.toSlug()}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()
                .responseBody
        responseBody!!.let {
            assertTrue(it.contains("<strong>bold</strong>"))
            assertTrue(it.contains("<ul>"))
            assertTrue(it.contains("<li>Item 1</li>"))
            assertTrue(it.contains("<a href=\"https://www.google.com\">Link to Google</a>"))
        }
    }

    @Test
    fun `Assert user profile page title, content and status code`() {
        val responseBody =
            restClient
                .get()
                .uri("${Routes.USER}/johnDoe")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()
                .responseBody
        responseBody!!.let {
            assertTrue(it.contains("John"))
            assertTrue(it.contains("Doe"))
            assertTrue(it.contains("Recent Articles by John"))
            assertTrue(it.contains("Lorem"))
        }
    }

    @Test
    fun `Assert category page title, content and status code`() {
        val slug = "frameworks" // Seeded in BlogConfiguration
        val responseBody =
            restClient
                .get()
                .uri("${Routes.CATEGORY}/$slug")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()
                .responseBody
        responseBody!!.let {
            assertTrue(it.contains("Category: Frameworks"))
            assertTrue(it.contains("Spring Security")) // Seeded article in Frameworks
        }
    }

    @Test
    fun `Assert tag page title, content and status code`() {
        val slug = "kotlin" // Seeded in BlogConfiguration
        val responseBody =
            restClient
                .get()
                .uri("${Routes.TAG}/$slug")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()
                .responseBody
        responseBody!!.let {
            assertTrue(it.contains("Tag: Kotlin"))
            assertTrue(it.contains("Kotlin Features")) // Seeded article with Kotlin
        }
    }
}
