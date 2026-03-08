package com.huangsam.springdemo.blog

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.log10
import org.springframework.stereotype.Service

@Service
class SearchService {
    private val index = ConcurrentHashMap<String, MutableMap<Long, Int>>()
    private val docLengths = ConcurrentHashMap<Long, Int>()
    private val idf = ConcurrentHashMap<String, Double>()
    private var totalDocs = 0

    fun index(articles: Iterable<Article>) {
        index.clear()
        docLengths.clear()
        idf.clear()
        val docs = articles.toList()
        totalDocs = docs.size

        if (totalDocs == 0) return

        for (article in docs) {
            val categoryPart = article.category?.name ?: ""
            val tagsPart = article.tags.joinToString(" ") { it.name }
            val content =
                "${article.title} ${article.headline} ${article.content} $categoryPart $tagsPart"
            val tokens = tokenize(content)
            docLengths[article.id!!] = tokens.size

            val termCounts = mutableMapOf<String, Int>()
            for (token in tokens) {
                termCounts[token] = termCounts.getOrDefault(token, 0) + 1
            }

            for ((term, count) in termCounts) {
                index.getOrPut(term) { ConcurrentHashMap() }[article.id!!] = count
            }
        }

        // Calculate IDF
        for (term in index.keys) {
            val docsWithTerm = index[term]?.size ?: 0
            idf[term] = log10(totalDocs.toDouble() / docsWithTerm.toDouble())
        }
    }

    fun search(query: String): List<Long> {
        if (query.isBlank() || totalDocs == 0) return emptyList()

        val queryTokens = tokenize(query)
        val scores = mutableMapOf<Long, Double>()

        for (token in queryTokens) {
            val termIdf = idf[token] ?: continue
            val docCounts = index[token] ?: continue

            for ((docId, count) in docCounts) {
                val tf = count.toDouble() / docLengths.getOrDefault(docId, 1).toDouble()
                val score = tf * termIdf
                scores[docId] = scores.getOrDefault(docId, 0.0) + score
            }
        }

        return scores.entries.sortedByDescending { it.value }.map { it.key }
    }

    fun findRelated(article: Article, limit: Int = 3): List<Long> {
        if (totalDocs == 0) return emptyList()

        val categoryPart = article.category?.name ?: ""
        val tagsPart = article.tags.joinToString(" ") { it.name }
        val seedContent = "${article.title} $categoryPart $tagsPart"
        val queryTokens = tokenize(seedContent)

        val scores = mutableMapOf<Long, Double>()
        for (token in queryTokens) {
            val termIdf = idf[token] ?: continue
            val docCounts = index[token] ?: continue

            for ((docId, count) in docCounts) {
                if (docId == article.id) continue // Exclude self
                val tf = count.toDouble() / docLengths.getOrDefault(docId, 1).toDouble()
                val score = tf * termIdf
                scores[docId] = scores.getOrDefault(docId, 0.0) + score
            }
        }

        return scores.entries.sortedByDescending { it.value }.take(limit).map { it.key }
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase().replace(Regex("[^a-z0-9\\s]"), " ").split(Regex("\\s+")).filter {
            it.isNotBlank()
        }
    }
}
