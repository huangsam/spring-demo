package com.huangsam.springdemo.blog

import com.huangsam.springdemo.Routes
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
internal class HtmlControllerTest
@Autowired
constructor(
    private val restClient: RestTestClient,
    private val articleRepository: ArticleRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @BeforeEach
    internal fun setUp() {
        logger.info("Set up HTML test")
    }

    @AfterEach
    internal fun tearDown() {
        logger.info("Tear down HTML test")
    }

    @Test
    internal fun `Assert blog page title, content and status code`() {
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
            assertTrue(it.contains("views"))
            assertTrue(it.contains("likes"))
        }
    }

    @Test
    internal fun `Assert blog pagination navigation works`() {
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
            assertTrue(
                it.contains("Page 1 of")
            ) // Since we have 100 posts, there are multiple pages
        }
    }

    @Test
    internal fun `Assert article page title, content and status code`() {
        logger.info("Assert article page works")
        val article = articleRepository.findAll().first()
        val responseBody =
            restClient
                .get()
                .uri("${Routes.ARTICLE}/${article.slug}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()
                .responseBody
        responseBody!!.let {
            assertTrue(it.contains("views"))
            assertTrue(it.contains("likes"))
        }
    }

    @Test
    internal fun `Assert markdown rendering works`() {
        // Our generated content has markdown (e.g., ## Introduction)
        val article = articleRepository.findAll().first()
        val responseBody =
            restClient
                .get()
                .uri("${Routes.ARTICLE}/${article.slug}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()
                .responseBody
        responseBody!!.let {
            // Check that the markdown header is rendered as an actual HTML header
            assertTrue(it.contains("<h2"))
            assertTrue(it.contains("Introduction"))
        }
    }

    @Test
    internal fun `Assert user profile page title, content and status code`() {
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
        }
    }

    @Test
    internal fun `Assert category page title, content and status code`() {
        val category = categoryRepository.findAll().first()
        val responseBody =
            restClient
                .get()
                .uri("${Routes.CATEGORY}/${category.slug}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()
                .responseBody
        responseBody!!.let { assertTrue(it.contains("Category: ${category.name}")) }
    }

    @Test
    internal fun `Assert tag page title, content and status code`() {
        val tag = tagRepository.findAll().first()
        val responseBody =
            restClient
                .get()
                .uri("${Routes.TAG}/${tag.slug}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()
                .responseBody
        responseBody!!.let { assertTrue(it.contains("Tag: ${tag.name}")) }
    }

    @Test
    internal fun `Assert search functionality works`() {
        val responseBody =
            restClient
                .get()
                .uri("${Routes.ROOT}?search=Kotlin")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()
                .responseBody
        responseBody!!.let {
            assertTrue(it.contains("Search Results"))
            assertTrue(it.contains("Results for \"Kotlin\""))
        }
    }
}
