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
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.jvm.Throws

@RestController
@RequestMapping(Routes.API_ARTICLE)
class ArticleController(private val repository: ArticleRepository, private val userRepository: UserRepository) {
    @GetMapping("/")
    fun findAll(): Iterable<Article> = repository.findAllWithAuthorOrderByAddedAtDesc()

    @Throws(ResponseStatusException::class)
    @GetMapping("/{slug}")
    fun findOne(@PathVariable slug: String): Article =
        repository.findBySlugWithAuthor(slug)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This article does not exist")

    @PostMapping("/")
    fun createArticle(@RequestBody articleRequest: ArticleRequest): Article {
        val auth = SecurityContextHolder.getContext().authentication
        val author = auth?.let { userRepository.findByLogin(it.name) }
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "You must be logged in to create an article")

        val article = Article(
            title = articleRequest.title,
            headline = articleRequest.headline,
            content = articleRequest.content,
            author = author
        )
        return repository.save(article)
    }
}

@RestController
@RequestMapping(Routes.API_USER)
class UserController(private val repository: UserRepository, private val passwordEncoder: PasswordEncoder) {
    @GetMapping("/")
    fun findAll(): Iterable<User> = repository.findAll()

    @Throws(ResponseStatusException::class)
    @GetMapping("/{login}")
    fun findOne(@PathVariable login: String): User =
        repository.findByLogin(login)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This user does not exist")

    @PostMapping("/register")
    fun register(@RequestBody registrationRequest: RegistrationRequest): User {
        if (repository.findByLogin(registrationRequest.login) != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Login already exists")
        }
        val user = User(
            login = registrationRequest.login,
            firstname = registrationRequest.firstname,
            lastname = registrationRequest.lastname,
            password = passwordEncoder.encode(registrationRequest.password) ?: throw IllegalArgumentException("Password cannot be null")
        )
        return repository.save(user)
    }
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
        return repository.findAllByArticleWithAuthorOrderByAddedAtDesc(article)
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

data class RegistrationRequest(
    val login: String,
    val firstname: String,
    val lastname: String,
    val password: String
)

data class ArticleRequest(
    val title: String,
    val headline: String,
    val content: String
)
