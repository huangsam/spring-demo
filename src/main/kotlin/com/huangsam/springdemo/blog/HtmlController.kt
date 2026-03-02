package com.huangsam.springdemo.blog

import com.huangsam.springdemo.Routes
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.server.ResponseStatusException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.AnonymousAuthenticationToken

private object MustacheView {
    const val BLOG = "blog"
    const val ARTICLE = "article"
}

@Controller
class HtmlController(
    private val repository: ArticleRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val markdownConverter: MarkdownConverter
) {
    @GetMapping(Routes.ROOT)
    fun blog(model: Model): String {
        model["title"] = "Blog"
        // use the explicit fetch-join variant to avoid N+1 when rendering
        model["articles"] = repository.findAllWithAuthorOrderByAddedAtDesc().map { it.render() }
        model["user"] = getAuthenticatedUser()
        return MustacheView.BLOG
    }

    @GetMapping("/login")
    fun login(model: Model): String {
        model["title"] = "Login"
        return "login"
    }

    @GetMapping("/register")
    fun register(model: Model): String {
        model["title"] = "Register"
        return "register"
    }

    @GetMapping("/new-article")
    fun newArticle(model: Model): String {
        model["title"] = "New Article"
        model["user"] = getAuthenticatedUser()
        return "new-article"
    }

    private fun getAuthenticatedUser(): User? {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null || !auth.isAuthenticated || auth is AnonymousAuthenticationToken) {
            return null
        }
        return (repository as? UserRepository)?.findByLogin(auth.name)
               ?: userRepository.findByLogin(auth.name)
    }

    @GetMapping("${Routes.ARTICLE}/{slug}")
    fun article(@PathVariable slug: String, model: Model): String {
        val article = repository
            .findBySlugWithAuthor(slug)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This article does not exist")
        model["title"] = article.title
        model["article"] = article.render()
        model["comments"] = commentRepository
            .findAllByArticleWithAuthorOrderByAddedAtDesc(article)
            .map { it.render() }
        model["user"] = getAuthenticatedUser()
        return MustacheView.ARTICLE
    }

    fun Article.render() = RenderedArticle(
        slug,
        title,
        headline,
        markdownConverter.convertToHtml(content),
        author,
        addedAt.format()
    )

    fun Comment.render() = RenderedComment(
        author,
        content,
        addedAt.format()
    )

    data class RenderedArticle(
        val slug: String,
        val title: String,
        val headline: String,
        val content: String,
        val author: User,
        val addedAt: String
    )

    data class RenderedComment(
        val author: User,
        val content: String,
        val addedAt: String
    )
}
