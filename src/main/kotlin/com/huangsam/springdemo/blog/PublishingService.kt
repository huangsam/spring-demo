package com.huangsam.springdemo.blog

import java.time.LocalDateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// Automatically publishes articles that have been scheduled for a future time.
// This demonstrates Spring's scheduling capability for background tasks.
@Service
class PublishingService(
    private val articleRepository: ArticleRepository,
    private val searchService: SearchService,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    // @Scheduled(fixedRate = 60000) runs this method every 60 seconds automatically.
    // @Transactional ensures database changes are committed as a single unit.
    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    fun publishScheduledArticles() {
        val now = LocalDateTime.now()
        // Find all draft articles whose scheduled publication time has arrived
        val scheduledArticles =
            articleRepository.findAllByStatusAndScheduledAtBefore(ArticleStatus.DRAFT, now)

        if (scheduledArticles.isNotEmpty()) {
            logger.info("Found ${scheduledArticles.size} articles to publish")
            // Update each article's status from DRAFT to PUBLISHED
            scheduledArticles.forEach { article ->
                article.status = ArticleStatus.PUBLISHED
                articleRepository.save(article)
                logger.info("Published article: ${article.title}")
            }

            // Re-index the search service so newly published articles appear in search results
            searchService.index(articleRepository.findAll())
        }
    }
}
