package com.huangsam.springdemo.blog

import com.huangsam.springdemo.Routes
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.server.ResponseStatusException
import org.springframework.security.core.context.SecurityContextHolder
import kotlin.jvm.Throws

@RestController
@RequestMapping(Routes.API_ARTICLE)
class ArticleController(private val repository: ArticleRepository) {
    @GetMapping("/")
    fun findAll(): Iterable<Article> = repository.findAllByOrderByAddedAtDesc()

    @Throws(ResponseStatusException::class)
    @GetMapping("/{slug}")
    fun findOne(@PathVariable slug: String): Article =
        repository.findBySlug(slug)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This article does not exist")
}

@RestController
@RequestMapping(Routes.API_USER)
class UserController(private val repository: UserRepository) {
    @GetMapping("/")
    fun findAll(): Iterable<User> = repository.findAll()

    @Throws(ResponseStatusException::class)
    @GetMapping("/{login}")
    fun findOne(@PathVariable login: String): User =
        repository.findByLogin(login)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This user does not exist")
}
@RestController
@RequestMapping(Routes.API_COMMENT)
class CommentController(
    private val repository: CommentRepository,
    private val articleRepository: ArticleRepository,
    private val userRepository: UserRepository
) {
    @GetMapping("/{slug}")
    fun findByArticle(@PathVariable slug: String): Iterable<Comment> {
        val article = articleRepository.findBySlug(slug)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This article does not exist")
        return repository.findAllByArticleOrderByAddedAtDesc(article)
    }

    @PostMapping("/")
    fun addComment(@RequestBody commentRequest: CommentRequest): Comment {
        val article = articleRepository.findBySlug(commentRequest.articleSlug)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This article does not exist")

        val auth = SecurityContextHolder.getContext().authentication
        val author = auth?.let { userRepository.findByLogin(it.name) }
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "You must be logged in to comment")

        return repository.save(Comment(article, author, commentRequest.content))
    }
}

data class CommentRequest(
    val articleSlug: String,
    val content: String
)
