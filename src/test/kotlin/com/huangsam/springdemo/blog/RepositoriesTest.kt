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
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
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

        val pageable = org.springframework.data.domain.PageRequest.of(0, 20)
        val found = articleRepository.findAllByOrderByAddedAtDesc(pageable).content
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

    @Test
    fun `When findBySlug then return Category`() {
        val category = Category("Frameworks")
        entityManager.persist(category)
        entityManager.flush()

        val found = categoryRepository.findBySlug(category.slug)!!
        assertEquals(category.name, found.name)
        assertEquals(category.slug, found.slug)
    }

    @Test
    fun `When findAllByOrderByNameAsc then return Categories sorted`() {
        val c1 = Category("Data")
        val c2 = Category("Frameworks")
        entityManager.persist(c1)
        entityManager.persist(c2)
        entityManager.flush()

        val found = categoryRepository.findAllByOrderByNameAsc().toList()
        assertEquals(2, found.size)
        assertEquals("Data", found[0].name)
        assertEquals("Frameworks", found[1].name)
    }

    @Test
    fun `When findBySlug then return Tag`() {
        val tag = Tag("Kotlin")
        entityManager.persist(tag)
        entityManager.flush()

        val found = tagRepository.findBySlug(tag.slug)!!
        assertEquals(tag.name, found.name)
        assertEquals(tag.slug, found.slug)
    }

    @Test
    fun `When findAllByOrderByNameAsc then return Tags sorted`() {
        val t1 = Tag("Spring")
        val t2 = Tag("JPA")
        entityManager.persist(t1)
        entityManager.persist(t2)
        entityManager.flush()

        val found = tagRepository.findAllByOrderByNameAsc().toList()
        assertEquals(2, found.size)
        assertEquals("JPA", found[0].name)
        assertEquals("Spring", found[1].name)
    }

    @Test
    fun `When findByNameIn then return matching Tags`() {
        val t1 = Tag("Spring")
        val t2 = Tag("Kotlin")
        val t3 = Tag("JPA")
        entityManager.persist(t1)
        entityManager.persist(t2)
        entityManager.persist(t3)
        entityManager.flush()

        val found = tagRepository.findByNameIn(listOf("Spring", "JPA")).toList()
        assertEquals(2, found.size)
        assertTrue(found.any { it.name == "Spring" })
        assertTrue(found.any { it.name == "JPA" })
    }

    @Test
    fun `When findAllByCategoryOrderByAddedAtDesc then return Articles in category`() {
        val user = johnDoe
        entityManager.persist(user)
        val category = Category("Frameworks")
        entityManager.persist(category)
        val now = LocalDateTime.now()
        val a1 =
            Article(
                "Article A",
                "Headline A",
                "content a",
                user,
                category = category,
                addedAt = now.minusSeconds(1),
            )
        val a2 =
            Article(
                "Article B",
                "Headline B",
                "content b",
                user,
                category = category,
                addedAt = now,
            )
        entityManager.persist(a1)
        entityManager.persist(a2)
        entityManager.flush()
        entityManager.clear()

        val found = articleRepository.findAllByCategoryOrderByAddedAtDesc(category).toList()
        assertEquals(2, found.size)
        // Verify desc ordering: most recent first
        assertEquals("Article B", found[0].title)
        assertEquals("Article A", found[1].title)
    }

    @Test
    fun `When findAllByTagsContainingOrderByAddedAtDesc then return Articles with tag`() {
        val user = johnDoe
        entityManager.persist(user)
        val tag = Tag("Spring")
        entityManager.persist(tag)
        val now = LocalDateTime.now()
        val a1 =
            Article(
                "Article A",
                "Headline A",
                "content a",
                user,
                tags = mutableSetOf(tag),
                addedAt = now.minusSeconds(1),
            )
        val a2 =
            Article(
                "Article B",
                "Headline B",
                "content b",
                user,
                tags = mutableSetOf(tag),
                addedAt = now,
            )
        entityManager.persist(a1)
        entityManager.persist(a2)
        entityManager.flush()
        entityManager.clear()

        val found = articleRepository.findAllByTagsContainingOrderByAddedAtDesc(tag).toList()
        assertEquals(2, found.size)
        // Verify desc ordering: most recent first
        assertEquals("Article B", found[0].title)
        assertEquals("Article A", found[1].title)
    }

    @Test
    fun `When Article has category and tags then findBySlug fetches them`() {
        val user = johnDoe
        entityManager.persist(user)
        val category = Category("Languages")
        entityManager.persist(category)
        val tag1 = Tag("Kotlin")
        val tag2 = Tag("JVM")
        entityManager.persist(tag1)
        entityManager.persist(tag2)
        val article =
            Article(
                "Kotlin Features",
                "Modern Java Alternative",
                "content",
                user,
                category = category,
                tags = mutableSetOf(tag1, tag2),
            )
        entityManager.persist(article)
        entityManager.flush()
        entityManager.clear()

        val found = articleRepository.findBySlug(article.slug)!!
        assertEquals(category.name, found.category!!.name)
        assertEquals(2, found.tags.size)
        assertTrue(found.tags.any { it.name == "Kotlin" })
        assertTrue(found.tags.any { it.name == "JVM" })
    }
}
