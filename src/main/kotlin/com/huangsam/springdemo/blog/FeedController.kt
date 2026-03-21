package com.huangsam.springdemo.blog

import com.rometools.rome.feed.synd.*
import com.rometools.rome.io.SyndFeedOutput
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
public class FeedController(private val repository: ArticleRepository) {

    @GetMapping("/rss", produces = ["application/rss+xml"])
    public fun rss(): String {
        return generateFeed("rss_2.0")
    }

    @GetMapping("/atom", produces = ["application/atom+xml"])
    public fun atom(): String {
        return generateFeed("atom_1.0")
    }

    private fun generateFeed(feedType: String): String {
        val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()

        val feed =
            SyndFeedImpl().apply {
                this.feedType = feedType
                title = "Spring Demo Blog"
                description = "A simple blog with Spring Boot and Kotlin"
                link = baseUrl
            }

        val articles =
            repository
                .findAllByStatusOrderByAddedAtDesc(ArticleStatus.PUBLISHED, PageRequest.of(0, 50))
                .content
        val entries = articles.map { article ->
            SyndEntryImpl().apply {
                title = article.title
                link = "$baseUrl/article/${article.slug}"
                publishedDate = article.addedAt.toDate()
                author = "${article.author.firstname} ${article.author.lastname}"
                val entryDescription =
                    SyndContentImpl().apply {
                        type = "text/plain"
                        value = article.headline
                    }
                this.description = entryDescription
            }
        }

        feed.entries = entries
        return SyndFeedOutput().outputString(feed)
    }
}
