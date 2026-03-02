package com.huangsam.springdemo.blog

import java.nio.charset.StandardCharsets
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
// This configuration class illustrates a few wiring patterns used in typical
// Spring Boot applications. In production you would normally keep these
// classes focused on creating and configuring infrastructure beans rather
// than inserting sample data; the `databaseInitializer` here is purely a demo
// convenience.
class BlogConfiguration {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    // Register a bean of type ApplicationRunner. Spring Boot will execute
    // all ApplicationRunner and CommandLineRunner beans after the context has
    // been refreshed. The lambda provided below becomes the implementation of
    // the single abstract method `run(ApplicationArguments)` (SAM conversion).
    //
    // NOTE: this pattern is handy for bootstrapping demo or development data but
    // it is not something you would use in a real production system where
    // content should be created via migrations, admin UI, or external import.
    // In a larger app this class would typically only declare service,
    // client and configuration beans, not direct database access logic.
    @Bean
    fun databaseInitializer(
        userRepository: UserRepository,
        articleRepository: ArticleRepository,
        passwordEncoder: PasswordEncoder,
    ) = ApplicationRunner {
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

        logger.info("Create articles that John Doe is an author of")
        articleRepository.save(
            Article(
                title = "Markdown Test",
                headline = "Testing Markdown Rendering",
                content = readResource("markdown/markdown-test.md"),
                author = johnDoe,
            )
        )
        articleRepository.save(
            Article(
                title = "Lorem",
                headline = "Lorem",
                content = "dolor sit amet",
                author = johnDoe,
            )
        )
        articleRepository.save(
            Article(
                title = "Spring Security",
                headline = "Securing your Spring Boot Apps",
                content = readResource("markdown/spring-security.md"),
                author = johnDoe,
            )
        )
        articleRepository.save(
            Article(
                title = "Kotlin Features",
                headline = "Modern Java Alternative",
                content = readResource("markdown/kotlin-features.md"),
                author = johnDoe,
            )
        )
        articleRepository.save(
            Article(
                title = "JPA and Hibernate",
                headline = "Persistence Made Easy",
                content = readResource("markdown/jpa-hibernate.md"),
                author = johnDoe,
            )
        )
    }

    private fun readResource(path: String): String {
        return ClassPathResource(path).inputStream.use {
            String(it.readAllBytes(), StandardCharsets.UTF_8)
        }
    }
}
