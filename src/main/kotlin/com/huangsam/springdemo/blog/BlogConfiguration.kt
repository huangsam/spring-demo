package com.huangsam.springdemo.blog

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BlogConfiguration {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun databaseInitializer(
        userRepository: UserRepository,
        articleRepository: ArticleRepository,
    ) = ApplicationRunner {
        logger.info("Create user John Doe")
        val johnDoe = userRepository.save(User("johnDoe", "John", "Doe"))

        logger.info("Create articles that John Doe is an author of")
        articleRepository.save(
            Article(
                title = "Lorem",
                headline = "Lorem",
                content = "dolor sit amet",
                author = johnDoe
            )
        )
        articleRepository.save(
            Article(
                title = "Ipsum",
                headline = "Ipsum",
                content = "dolor sit amet",
                author = johnDoe
            )
        )
    }
}
