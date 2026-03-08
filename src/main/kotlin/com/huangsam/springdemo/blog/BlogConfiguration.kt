package com.huangsam.springdemo.blog

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.security.crypto.password.PasswordEncoder

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeedUser(
    val login: String,
    val firstname: String,
    val lastname: String,
    val description: String?,
    val password: String,
    val role: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeedCategory(val name: String, val slug: String)

@JsonIgnoreProperties(ignoreUnknown = true) data class SeedTag(val name: String, val slug: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeedComment(val author: SeedUser, val content: String, val addedAt: LocalDateTime)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeedArticle(
    val title: String,
    val headline: String,
    val content: String,
    val author: SeedUser,
    val category: SeedCategory?,
    val tags: List<SeedTag> = emptyList(),
    val comments: List<SeedComment> = emptyList(),
    val slug: String,
    val addedAt: LocalDateTime,
    val views: Int,
    val likes: Int,
)

@Configuration
class BlogConfiguration {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    // ApplicationRunner boots up the database with seed data on application startup.
    @Bean
    @org.springframework.transaction.annotation.Transactional
    fun databaseInitializer(
        userRepository: UserRepository,
        articleRepository: ArticleRepository,
        categoryRepository: CategoryRepository,
        tagRepository: TagRepository,
        commentRepository: CommentRepository,
        passwordEncoder: PasswordEncoder,
        searchService: SearchService,
    ) = ApplicationRunner {
        if (userRepository.findByLogin("admin") == null) {
            logger.info("Create admin user")
            userRepository.save(
                User(
                    login = "admin",
                    firstname = "Admin",
                    lastname = "User",
                    description = "System Administrator",
                    password = passwordEncoder.encode("admin")!!,
                    role = "ADMIN",
                )
            )
        }

        logger.info("Create user John Doe")
        val johnDoe =
            userRepository.save(
                User(
                    login = "johnDoe",
                    firstname = "John",
                    lastname = "Doe",
                    description = null,
                    password = passwordEncoder.encode("johnDoe")!!,
                    role = "USER",
                )
            )

        logger.info("Loading seed data from JSON")
        val json = readResource("seed-data.json")
        val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        val seedArticles: List<SeedArticle> = mapper.readValue(json)

        val persistedUsers = mutableMapOf<String, User>("johnDoe" to johnDoe)
        val persistedCategories = mutableMapOf<String, Category>()
        val persistedTags = mutableMapOf<String, Tag>()

        for (seedArticle in seedArticles) {
            val author =
                persistedUsers.getOrPut(seedArticle.author.login) {
                    userRepository.save(
                        User(
                            login = seedArticle.author.login,
                            firstname = seedArticle.author.firstname,
                            lastname = seedArticle.author.lastname,
                            description = seedArticle.author.description,
                            password = passwordEncoder.encode(seedArticle.author.password)!!,
                            role = seedArticle.author.role,
                        )
                    )
                }

            var category: Category? = null
            if (seedArticle.category != null) {
                category =
                    persistedCategories.getOrPut(seedArticle.category.slug) {
                        categoryRepository.save(
                            Category(
                                name = seedArticle.category.name,
                                slug = seedArticle.category.slug,
                            )
                        )
                    }
            }

            val articleTags = mutableSetOf<Tag>()
            for (seedTag in seedArticle.tags) {
                val tag =
                    persistedTags.getOrPut(seedTag.slug) {
                        tagRepository.save(Tag(name = seedTag.name, slug = seedTag.slug))
                    }
                articleTags.add(tag)
            }

            val article =
                articleRepository.save(
                    Article(
                        title = seedArticle.title,
                        headline = seedArticle.headline,
                        content = seedArticle.content,
                        author = author,
                        category = category,
                        tags = articleTags,
                        slug = seedArticle.slug,
                        addedAt = seedArticle.addedAt,
                        views = seedArticle.views,
                        likes = seedArticle.likes,
                        status = ArticleStatus.PUBLISHED,
                    )
                )

            for (seedComment in seedArticle.comments) {
                val commentAuthor =
                    persistedUsers.getOrPut(seedComment.author.login) {
                        userRepository.save(
                            User(
                                login = seedComment.author.login,
                                firstname = seedComment.author.firstname,
                                lastname = seedComment.author.lastname,
                                description = seedComment.author.description,
                                password = passwordEncoder.encode(seedComment.author.password)!!,
                                role = seedComment.author.role,
                            )
                        )
                    }
                commentRepository.save(
                    Comment(
                        article = article,
                        author = commentAuthor,
                        content = seedComment.content,
                        addedAt = seedComment.addedAt,
                    )
                )
            }
        }

        logger.info("Indexing articles for search")
        searchService.index(articleRepository.findAll())

        logger.info("Finished loading seed data")
    }

    private fun readResource(path: String): String {
        return ClassPathResource(path).inputStream.use {
            String(it.readAllBytes(), StandardCharsets.UTF_8)
        }
    }
}
