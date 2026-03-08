#!/usr/bin/env kotlin

@file:DependsOn("net.datafaker:datafaker:2.0.2")
@file:DependsOn("com.fasterxml.jackson.core:jackson-databind:2.15.2")
@file:DependsOn("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
@file:DependsOn("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.datafaker.Faker
import java.io.File
import java.text.Normalizer
import java.time.LocalDateTime
import java.util.*
import java.util.regex.Pattern

val faker = Faker()

fun String.toSlug() =
    // Normalize unicode; convert accented chars (é, ñ) to base form (e, n) for ASCII compatibility
    Normalizer.normalize(this, Normalizer.Form.NFD)
        // Remove all non-word characters except spaces and hyphens (e.g., punctuation, special chars)
        .replace(Regex("[^\\w\\s-]"), "")
        // Replace multiple consecutive hyphens with a single space (e.g., "---" becomes " ")
        .replace(Regex("-+"), " ")
        .trim()
        // Replace multiple consecutive spaces with a single hyphen (e.g., "  " becomes "-")
        .replace(Regex("\\s+"), "-")
        .lowercase(Locale.ENGLISH)

data class Tag(val name: String, val slug: String = name.toSlug())
data class Category(val name: String, val slug: String = name.toSlug())
data class User(
    val login: String,
    val firstname: String,
    val lastname: String,
    val description: String?,
    val password: String = "password", // Dummy password
    val role: String = "USER"
)
data class Comment(
    val author: User,
    val content: String,
    val addedAt: LocalDateTime
)

data class Article(
    val title: String,
    val headline: String,
    val content: String,
    val author: User,
    val category: Category?,
    val tags: List<Tag>,
    val comments: List<Comment>,
    val slug: String = title.toSlug(),
    val addedAt: LocalDateTime = LocalDateTime.now(),
    val views: Int = 0,
    val likes: Int = 0
)

fun main() {
    println("Generating seed data...")

    val users = (1..15).map {
        User(
            login = faker.name().username(),
            firstname = faker.name().firstName(),
            lastname = faker.name().lastName(),
            description = faker.lorem().sentence()
        )
    }

    val categories = listOf(
        "Spring Boot", "Kotlin", "Java", "Web Development", "Microservices",
        "DevOps", "Testing", "Architecture", "Security"
    ).map { Category(it) }

    val tags = (1..20).map { Tag(faker.hacker().noun()) }

    val uniqueTitles = generateSequence { faker.book().title() }.distinct().take(100).toList()

    val articles = (0 until 100).map { index ->
        val title = uniqueTitles[index]
        val author = users.random()
        val category = categories.random()
        val articleTags = tags.shuffled().take((1..4).random())
        val addedAt = LocalDateTime.now().minusDays(faker.number().numberBetween(1L, 365L))

        // Generate several paragraphs of content using markdown
        val paragraphs = (1..5).joinToString("\n\n") { faker.lorem().paragraph(10) }
        val content = "## Introduction\n\n$paragraphs\n\n## Conclusion\n\n${faker.lorem().paragraph(5)}"

        val articleComments = (0..faker.number().numberBetween(0, 10)).map {
            Comment(
                author = users.random(),
                content = faker.lorem().paragraph((1..3).random()),
                // Stagger comments 1-240 hours after article publication for realistic discussion timeline
                addedAt = addedAt.plusHours(faker.number().numberBetween(1L, 240L))
            )
        }.sortedBy { it.addedAt }

        Article(
            title = title,
            headline = faker.lorem().sentence(10),
            content = content,
            author = author,
            category = category,
            tags = articleTags,
            comments = articleComments,
            addedAt = addedAt,
            views = faker.number().numberBetween(10, 5000),
            likes = faker.number().numberBetween(0, 500)
        )
    }

    // Sort by addedAt descending
    val sortedArticles = articles.sortedByDescending { it.addedAt }

    val mapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .enable(SerializationFeature.INDENT_OUTPUT)

    val outputFile = File("src/main/resources/seed-data.json")
    outputFile.parentFile.mkdirs()
    mapper.writeValue(outputFile, sortedArticles)

    println("Successfully generated ${sortedArticles.size} articles at ${outputFile.path}")
}

main()
