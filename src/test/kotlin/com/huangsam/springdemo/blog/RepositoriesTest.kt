package com.huangsam.springdemo.blog

import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.data.repository.findByIdOrNull

@DataJpaTest
class RepositoriesTest
@Autowired
constructor(
    private val entityManager: TestEntityManager,
    private val userRepository: UserRepository,
    private val articleRepository: ArticleRepository,
    private val commentRepository: CommentRepository,
) {
    private val johnDoe: User
        get() = User("johnDoe", "John", "Doe", password = "password")

    @Test
    fun `When findByIdOrNull then return Article`() {
        val user = johnDoe
        entityManager.persist(user)
        val article = Article("Lorem", "Lorem", "dolor sit amet", user)
        entityManager.persist(article)
        entityManager.flush()
        val found = articleRepository.findByIdOrNull(article.id!!)
        assertEquals(article, found)
    }

    @Test
    fun `When findByLogin then return User`() {
        val user = johnDoe
        entityManager.persist(user)
        entityManager.flush()
        val found = userRepository.findByLogin(user.login)!!
        assertEquals(user.firstname, found.firstname)
        assertEquals(user.lastname, found.lastname)
        assertEquals(user.description, found.description)
    }

    @Test
    fun `When findBySlug then fetches Article and author`() {
        val user = johnDoe
        entityManager.persist(user)
        val article = Article("Lorem", "Lorem", "dolor sit amet", user)
        entityManager.persist(article)
        entityManager.flush()
        entityManager.clear()

        val found = articleRepository.findBySlug(article.slug)!!
        assertEquals(article.slug, found.slug)
        assertEquals(user.login, found.author.login)
    }

    @Test
    fun `When findAllByFirstnameOrderByLastnameAsc then return Users`() {
        val user1 = johnDoe
        val user2 = User("johnPike", "John", "Pike", password = "password")
        entityManager.persist(user1)
        entityManager.persist(user2)
        val found = userRepository.findAllByFirstnameOrderByLastnameAsc(user1.firstname)
        assertEquals(2, found.count())
        assertEquals(user1, found.first())
        assertEquals(user2, found.last())
        assertTrue(found.all { user -> user.firstname == user1.firstname })
    }

    @Test
    fun `When findAllByArticleOrderByAddedAtDesc then return Comments`() {
        val user = johnDoe
        entityManager.persist(user)
        val article = Article("Lorem", "Lorem", "dolor sit amet", user)
        entityManager.persist(article)
        val now = LocalDateTime.now()
        val comment1 = Comment(article, user, "First comment", addedAt = now.minusSeconds(1))
        val comment2 = Comment(article, user, "Second comment", addedAt = now)
        entityManager.persist(comment1)
        entityManager.persist(comment2)
        entityManager.flush()
        val found = commentRepository.findAllByArticleOrderByAddedAtDesc(article).toList()
        assertEquals(2, found.size)
        // verify ordering desc: most recent first
        assertEquals(comment2, found[0])
        assertEquals(comment1, found[1])
        assertTrue(found.all { it.article == article })
    }

    @Test
    fun `When findAllByOrderByAddedAtDesc then return Articles with authors`() {
        val user = johnDoe
        entityManager.persist(user)
        val article = Article("Lorem", "Lorem", "dolor sit amet", user)
        entityManager.persist(article)
        entityManager.flush()
        entityManager.clear() // detach so we can tell if author came back

        val found = articleRepository.findAllByOrderByAddedAtDesc().toList()
        assertEquals(1, found.size)
        assertEquals(article.title, found.first().title)
        assertEquals(user.login, found.first().author.login)
    }

    @Test
    fun `When findAllByArticleOrderByAddedAtDesc then return Comments with authors`() {
        val user = johnDoe
        entityManager.persist(user)
        val article = Article("Lorem", "Lorem", "dolor sit amet", user)
        entityManager.persist(article)
        val now = LocalDateTime.now()
        val comment1 = Comment(article, user, "First comment", addedAt = now.minusSeconds(1))
        val comment2 = Comment(article, user, "Second comment", addedAt = now)
        entityManager.persist(comment1)
        entityManager.persist(comment2)
        entityManager.flush()
        entityManager.clear()

        val found = commentRepository.findAllByArticleOrderByAddedAtDesc(article).toList()
        assertEquals(2, found.size)
        // verify ordering desc and that associated entities are fetched
        assertEquals(comment2.content, found[0].content)
        assertEquals(comment1.content, found[1].content)
        assertTrue(found.all { it.article.id == article.id })
        assertTrue(found.all { it.author.login == user.login })
    }
}
