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
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HtmlControllerTest(@Autowired private val restTemplate: TestRestTemplate) {
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
        val entity = restTemplate.getForEntity<String>(Routes.ROOT)
        assertEquals(HttpStatus.OK, entity.statusCode)
        entity.body!!.let {
            assertTrue(it.contains("<h1>Blog</h1>"))
            assertTrue(it.contains("Lorem"))
        }
    }

    @Test
    fun `Assert article page title, content and status code`() {
        logger.info("Assert article page works")
        val title = "Lorem"
        val entity = restTemplate.getForEntity<String>("${Routes.ARTICLE}/${title.toSlug()}")
        assertEquals(HttpStatus.OK, entity.statusCode)
        entity.body!!.let {
            assertTrue(it.contains(title))
            assertTrue(it.contains("Lorem"))
            assertTrue(it.contains("dolor sit amet"))
        }
    }
}
