package com.huangsam.springdemo.blog

import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/admin")
public class AdminController(
    private val articleRepository: ArticleRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val categoryRepository: CategoryRepository,
) {

    private fun getAuthenticatedUser(): User? {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null || !auth.isAuthenticated || auth is AnonymousAuthenticationToken) {
            return null
        }
        return userRepository.findByLogin(auth.name)
    }

    @GetMapping
    public fun dashboard(model: Model): String {
        val user = getAuthenticatedUser()
        model["title"] = "Admin Dashboard"
        model["articlesCount"] = articleRepository.count()
        model["usersCount"] = userRepository.count()
        model["commentsCount"] = commentRepository.count()

        // Analytics: Articles by category
        val categories = categoryRepository.findAll()
        val articlesByCategory =
            categories
                .map { category ->
                    mapOf(
                        "name" to category.name,
                        "count" to
                            articleRepository
                                .findAllByCategoryAndStatusOrderByAddedAtDesc(
                                    category,
                                    ArticleStatus.PUBLISHED,
                                )
                                .count(),
                    )
                }
                .filter { it["count"] as Int > 0 }
        model["articlesByCategory"] = articlesByCategory

        // Analytics: Recent comments
        model["recentComments"] =
            commentRepository.findAll().sortedByDescending { it.addedAt }.take(5)

        model["user"] = user
        model["isAdmin"] = user?.role == "ADMIN"
        return "admin-dashboard"
    }

    @GetMapping("/articles")
    public fun articles(model: Model, @RequestParam(required = false) title: String?): String {
        val user = getAuthenticatedUser()
        model["title"] = "Manage Articles"
        val articles =
            if (title != null && title.isNotBlank()) {
                articleRepository.findAllByTitleContainingIgnoreCase(title)
            } else {
                articleRepository.findAll()
            }
        model["articles"] = articles.map { it.render() }
        model["filterTitle"] = title ?: ""
        model["user"] = user
        model["isAdmin"] = user?.role == "ADMIN"
        return "admin-articles"
    }

    @GetMapping("/users")
    public fun users(model: Model, @RequestParam(required = false) login: String?): String {
        val user = getAuthenticatedUser()
        model["title"] = "Manage Users"
        val users =
            if (login != null && login.isNotBlank()) {
                userRepository.findAllByLoginContainingIgnoreCase(login)
            } else {
                userRepository.findAll()
            }
        model["users"] = users
        model["filterLogin"] = login ?: ""
        model["user"] = user
        model["isAdmin"] = user?.role == "ADMIN"
        return "admin-users"
    }

    @GetMapping("/comments")
    public fun comments(model: Model, @RequestParam(required = false) content: String?): String {
        val user = getAuthenticatedUser()
        model["title"] = "Manage Comments"
        val comments =
            if (content != null && content.isNotBlank()) {
                commentRepository.findAllByContentContainingIgnoreCase(content)
            } else {
                commentRepository.findAll()
            }
        model["comments"] = comments
        model["filterContent"] = content ?: ""
        model["user"] = user
        model["isAdmin"] = user?.role == "ADMIN"
        return "admin-comments"
    }

    @PostMapping("/articles/delete/{id}")
    public fun deleteArticle(@PathVariable id: Long): String {
        articleRepository.deleteById(id)
        return "redirect:/admin/articles"
    }

    @PostMapping("/users/delete/{id}")
    public fun deleteUser(@PathVariable id: Long): String {
        userRepository.deleteById(id)
        return "redirect:/admin/users"
    }

    @PostMapping("/comments/delete/{id}")
    public fun deleteComment(@PathVariable id: Long): String {
        commentRepository.deleteById(id)
        return "redirect:/admin/comments"
    }

    private fun Article.render() =
        RenderedArticle(id!!, slug, title, headline, content, author, addedAt.format())

    public data class RenderedArticle(
        public val id: Long,
        public val slug: String,
        public val title: String,
        public val headline: String,
        public val content: String,
        public val author: User,
        public val addedAt: String,
    )
}
