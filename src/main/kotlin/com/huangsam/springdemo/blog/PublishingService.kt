package com.huangsam.springdemo.blog

import java.time.LocalDateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PublishingService(
    private val articleRepository: ArticleRepository,
    private val searchService: SearchService,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    fun publishScheduledArticles() {
        val now = LocalDateTime.now()
        val scheduledArticles =
            articleRepository.findAllByStatusAndScheduledAtBefore(ArticleStatus.DRAFT, now)

        if (scheduledArticles.isNotEmpty()) {
            logger.info("Found ${scheduledArticles.size} articles to publish")
            scheduledArticles.forEach { article ->
                article.status = ArticleStatus.PUBLISHED
                articleRepository.save(article)
                logger.info("Published article: ${article.title}")
            }

            // Re-index search to include newly published articles
            searchService.index(articleRepository.findAll())
        }
    }
}
