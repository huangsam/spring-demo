package com.huangsam.springdemo.blog

import com.huangsam.springdemo.Routes
import kotlin.jvm.Throws
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping(Routes.API_ARTICLE)
public class ArticleController(
    private val repository: ArticleRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
) {
    @GetMapping("/")
    public fun findAll(
        @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") page: Int,
        @org.springframework.web.bind.annotation.RequestParam(required = false) category: String?,
        @org.springframework.web.bind.annotation.RequestParam(required = false) tag: String?,
        @org.springframework.web.bind.annotation.RequestParam(required = false) author: String?,
    ): Iterable<Article> {
        if (category != null) {
            val categoryEntity =
                categoryRepository.findBySlug(category)
                    ?: throw ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "This category does not exist",
                    )
            return repository.findAllByCategoryAndStatusOrderByAddedAtDesc(
                categoryEntity,
                ArticleStatus.PUBLISHED,
            )
        }
        if (tag != null) {
            val tagEntity =
                tagRepository.findBySlug(tag)
                    ?: throw ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "This tag does not exist",
                    )
            return repository.findAllByTagsContainingAndStatusOrderByAddedAtDesc(
                tagEntity,
                ArticleStatus.PUBLISHED,
            )
        }
        if (author != null) {
            val authorEntity =
                userRepository.findByLogin(author)
                    ?: throw ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "This user does not exist",
                    )
            return repository.findTop5ByAuthorAndStatusOrderByAddedAtDesc(
                authorEntity,
                ArticleStatus.PUBLISHED,
            )
        }

        val pageable = org.springframework.data.domain.PageRequest.of(page, 20)
        return repository
            .findAllByStatusOrderByAddedAtDesc(ArticleStatus.PUBLISHED, pageable)
            .content
    }

    @Throws(ResponseStatusException::class)
    @GetMapping("/{slug}")
    public fun findOne(@PathVariable slug: String): Article {
        val article =
            repository.findBySlug(slug)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This article does not exist",
                )

        val auth = SecurityContextHolder.getContext().authentication
        val currentUser = auth?.let { userRepository.findByLogin(it.name) }

        if (article.status == ArticleStatus.DRAFT && article.author.login != currentUser?.login) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "This article is not yet published")
        }
        return article
    }

    @PostMapping("/")
    public fun createArticle(@RequestBody articleRequest: ArticleRequest): Article {
        // Extract the currently authenticated user from Spring Security's context.
        // SecurityContextHolder is a thread-local holder of the security principal.
        val auth = SecurityContextHolder.getContext().authentication
        val author =
            auth?.let { userRepository.findByLogin(it.name) }
                ?: throw ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You must be logged in to create an article",
                )

        // Resolve category by name (find-or-create)
        val category =
            articleRequest.category
                ?.takeIf { it.isNotBlank() }
                ?.let { name ->
                    categoryRepository.findByName(name) ?: categoryRepository.save(Category(name))
                }

        // Resolve tags by name (find-or-create each)
        val tags =
            articleRequest.tags
                .filter { it.isNotBlank() }
                .map { name -> tagRepository.findByName(name) ?: tagRepository.save(Tag(name)) }
                .toMutableSet()

        val article =
            Article(
                title = articleRequest.title,
                headline = articleRequest.headline,
                content = articleRequest.content,
                author = author,
                category = category,
                tags = tags,
                status = articleRequest.status ?: ArticleStatus.DRAFT,
                scheduledAt = articleRequest.scheduledAt,
            )
        return repository.save(article)
    }

    @PostMapping("/{slug}/like")
    public fun likeArticle(
        @PathVariable slug: String
    ): org.springframework.http.ResponseEntity<Map<String, Int>> {
        val article =
            repository.findBySlug(slug)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This article does not exist",
                )

        article.likes++
        repository.save(article)

        return org.springframework.http.ResponseEntity.ok(mapOf("likes" to article.likes))
    }
}

@RestController
@RequestMapping(Routes.API_USER)
public class UserController(
    private val repository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @GetMapping("/") public fun findAll(): Iterable<User> = repository.findAll()

    @Throws(ResponseStatusException::class)
    @GetMapping("/{login}")
    public fun findOne(@PathVariable login: String): User =
        repository.findByLogin(login)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This user does not exist")

    @PostMapping("/register")
    public fun register(
        @RequestBody registrationRequest: RegistrationRequest
    ): org.springframework.http.ResponseEntity<User> {
        if (repository.findByLogin(registrationRequest.login) != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Login already exists")
        }
        val user =
            User(
                login = registrationRequest.login,
                firstname = registrationRequest.firstname,
                lastname = registrationRequest.lastname,
                password =
                    passwordEncoder.encode(registrationRequest.password)
                        ?: throw IllegalArgumentException("Password cannot be null"),
            )
        val saved = repository.save(user)
        return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(saved)
    }
}

@RestController
@RequestMapping(Routes.API_COMMENT)
public class CommentController(
    private val repository: CommentRepository,
    private val articleRepository: ArticleRepository,
    private val userRepository: UserRepository,
) {
    @GetMapping("/{slug}")
    public fun findByArticle(@PathVariable slug: String): Iterable<Comment> {
        val article =
            articleRepository.findBySlug(slug)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This article does not exist",
                )
        return repository.findAllByArticleOrderByAddedAtDesc(article)
    }

    @PostMapping("/")
    public fun addComment(@RequestBody commentRequest: CommentRequest): Comment {
        val article =
            articleRepository.findBySlug(commentRequest.articleSlug)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This article does not exist",
                )

        val auth = SecurityContextHolder.getContext().authentication
        val author =
            auth?.let { userRepository.findByLogin(it.name) }
                ?: throw ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You must be logged in to comment",
                )

        return repository.save(Comment(article, author, commentRequest.content))
    }
}

public data class CommentRequest(public val articleSlug: String, public val content: String)

public data class RegistrationRequest(
    public val login: String,
    public val firstname: String,
    public val lastname: String,
    public val password: String,
)

public data class ArticleRequest(
    public val title: String,
    public val headline: String,
    public val content: String,
    public val category: String? = null,
    public val tags: List<String> = emptyList(),
    public val status: ArticleStatus? = null,
    public val scheduledAt: java.time.LocalDateTime? = null,
)

@RestController
@RequestMapping(Routes.API_CATEGORY)
public class CategoryController(private val repository: CategoryRepository) {
    @GetMapping("/") public fun findAll(): Iterable<Category> = repository.findAllByOrderByNameAsc()
}

@RestController
@RequestMapping(Routes.API_TAG)
public class TagController(private val repository: TagRepository) {
    @GetMapping("/") public fun findAll(): Iterable<Tag> = repository.findAllByOrderByNameAsc()
}
