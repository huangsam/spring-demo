package com.huangsam.springdemo.blog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SearchServiceTest {

    private val searchService = SearchService()

    @Test
    fun `When indexing and searching then return ranked results`() {
        val user = User("login", "First", "Last", password = "password")
        val a1 =
            Article("Kotlin Basics", "Learn Kotlin", "Kotlin is a modern language", user).apply {
                id = 1L
            }
        val a2 =
            Article(
                    "Spring Boot",
                    "Learn Spring",
                    "Spring Boot makes it easy to create Stand-alone applications",
                    user,
                )
                .apply { id = 2L }
        val a3 =
            Article(
                    "Kotlin and Spring",
                    "Powerful duo",
                    "Kotlin and Spring together are great",
                    user,
                )
                .apply { id = 3L }

        searchService.index(listOf(a1, a2, a3))

        // Search for "Kotlin" - a1 and a3 should be returned, a1 ranked higher because it has more
        // "Kotlin" mentions relative to length?
        // Actually a1 has "Kotlin" in title and headline. a3 has "Kotlin" in title and content.
        val results = searchService.search("Kotlin")
        assertEquals(2, results.size)
        assertTrue(results.contains(1L))
        assertTrue(results.contains(3L))

        // Search for "Spring"
        val results2 = searchService.search("Spring")
        assertEquals(2, results2.size)
        assertTrue(results2.contains(2L))
        assertTrue(results2.contains(3L))

        // Search for both
        val results3 = searchService.search("Kotlin Spring")
        assertEquals(3, results3.size)
        // a3 should be ranked high as it contains both
        assertEquals(3L, results3[0])
    }

    @Test
    fun `When finding related articles then exclude self and rank by similarity`() {
        val user = User("login", "First", "Last", password = "password")
        val cat1 = Category("Kotlin")
        val cat2 = Category("Spring")

        val a1 = Article("Kotlin Basics", "Learn Kotlin", "Content 1", user, cat1).apply { id = 1L }
        val a2 = Article("Advanced Kotlin", "Deep dive", "Content 2", user, cat1).apply { id = 2L }
        val a3 = Article("Spring Boot", "Learn Spring", "Content 3", user, cat2).apply { id = 3L }
        val a4 = Article("Kotlin and Spring", "Duo", "Content 4", user, cat1).apply { id = 4L }

        searchService.index(listOf(a1, a2, a3, a4))

        // Related to a1 (Kotlin Basics, Category: Kotlin)
        // a2 and a4 share the category "Kotlin" and the word "Kotlin" in title.
        // a3 shares nothing.
        val related = searchService.findRelated(a1, limit = 2)

        assertEquals(2, related.size)
        assertTrue(related.contains(2L))
        assertTrue(related.contains(4L))
        assertTrue(!related.contains(1L)) // Exclude self
    }

    @Test
    fun `When searching with blank query then return empty list`() {
        searchService.index(emptyList())
        val results = searchService.search("")
        assertTrue(results.isEmpty())
    }

    @Test
    fun `When searching for non-existent word then return empty list`() {
        val user = User("login", "First", "Last", password = "password")
        val a1 = Article("Title", "Headline", "Content", user).apply { id = 1L }
        searchService.index(listOf(a1))

        val results = searchService.search("xyz123")
        assertTrue(results.isEmpty())
    }
}
