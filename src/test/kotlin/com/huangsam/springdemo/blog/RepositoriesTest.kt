package com.huangsam.springdemo.blog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.data.repository.findByIdOrNull

@DataJpaTest
class RepositoriesTest @Autowired constructor(
    private val entityManager: TestEntityManager,
    private val userRepository: UserRepository,
    private val articleRepository: ArticleRepository,
    private val commentRepository: CommentRepository
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
        val comment1 = Comment(article, user, "First comment")
        val comment2 = Comment(article, user, "Second comment")
        entityManager.persist(comment1)
        entityManager.persist(comment2)
        entityManager.flush()
        val found = commentRepository.findAllByArticleOrderByAddedAtDesc(article).toList()
        assertEquals(2, found.size)
        assertTrue(found.all { it.article == article })
    }
}
