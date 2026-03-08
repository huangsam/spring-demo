package com.huangsam.springdemo.blog

import com.huangsam.springdemo.Routes
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException

private object MustacheView {
    const val BLOG = "blog"
    const val ARTICLE = "article"
    const val CATEGORY = "category"
    const val TAG = "tag"
    const val PROFILE = "profile"
}

@Controller
class HtmlController(
    private val repository: ArticleRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
    private val markdownConverter: MarkdownConverter,
) {
    @GetMapping(Routes.ROOT)
    fun blog(@RequestParam(defaultValue = "0") page: Int, model: Model): String {
        model["title"] = "Blog"

        val pageSize = 5
        val pageable = PageRequest.of(page, pageSize)
        val articlePage = repository.findAllByOrderByAddedAtDesc(pageable)

        // author is fetched via EntityGraph in repository, preventing N+1
        model["articles"] = articlePage.content.map { it.render() }

        model["currentPage"] = page
        model["currentPageDisplay"] = page + 1
        model["totalPages"] = articlePage.totalPages
        model["isFirst"] = articlePage.isFirst()
        model["isLast"] = articlePage.isLast()
        model["hasNext"] = articlePage.hasNext()
        model["hasPrevious"] = articlePage.hasPrevious()
        model["nextPage"] = page + 1
        model["previousPage"] = page - 1
        model["lastPage"] = if (articlePage.totalPages > 0) articlePage.totalPages - 1 else 0

        val categories = categoryRepository.findAllByOrderByNameAsc().toList()
        model["categories"] = categories
        model["hasCategories"] = categories.isNotEmpty()

        val allTags = tagRepository.findAllByOrderByNameAsc().toList()
        model["allTags"] = allTags
        model["hasTags"] = allTags.isNotEmpty()
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

    // Helper used by both blog and article views to render the
    // currently logged-in user (if any). We look at the Spring Security
    // context, guard against anonymous authentication, and then resolve the
    // user from whichever repository instance is available (some tests wire a
    // mock ArticleRepository that also implements UserRepository).
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
        val article =
            repository.findBySlug(slug)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This article does not exist",
                )

        // Increment views and save
        article.views++
        repository.save(article)

        model["title"] = article.title
        model["article"] = article.render()
        model["comments"] =
            commentRepository.findAllByArticleOrderByAddedAtDesc(article).map { it.render() }
        model["user"] = getAuthenticatedUser()
        return MustacheView.ARTICLE
    }

    @GetMapping("${Routes.CATEGORY}/{slug}")
    fun category(@PathVariable slug: String, model: Model): String {
        val category =
            categoryRepository.findBySlug(slug)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This category does not exist",
                )
        model["title"] = "Category: ${category.name}"
        model["category"] = category
        model["articles"] =
            repository.findAllByCategoryOrderByAddedAtDesc(category).map { it.render() }
        model["user"] = getAuthenticatedUser()
        return MustacheView.CATEGORY
    }

    @GetMapping("${Routes.TAG}/{slug}")
    fun tag(@PathVariable slug: String, model: Model): String {
        val tag =
            tagRepository.findBySlug(slug)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This tag does not exist")
        model["title"] = "Tag: ${tag.name}"
        model["tag"] = tag
        model["articles"] =
            repository.findAllByTagsContainingOrderByAddedAtDesc(tag).map { it.render() }
        model["user"] = getAuthenticatedUser()
        return MustacheView.TAG
    }

    @GetMapping("${Routes.USER}/{login}")
    fun profile(@PathVariable login: String, model: Model): String {
        val profileUser =
            userRepository.findByLogin(login)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This user does not exist")
        model["title"] = "${profileUser.firstname} ${profileUser.lastname}"
        model["profileUser"] = profileUser
        model["articles"] =
            repository.findTop5ByAuthorOrderByAddedAtDesc(profileUser).map { it.render() }
        model["user"] = getAuthenticatedUser()
        return MustacheView.PROFILE
    }

    fun Article.render() =
        RenderedArticle(
            slug,
            title,
            headline,
            markdownConverter.convertToHtml(content),
            author,
            addedAt.format(),
            category,
            tags,
            views,
            likes,
        )

    fun Comment.render() = RenderedComment(author, content, addedAt.format())

    data class RenderedArticle(
        val slug: String,
        val title: String,
        val headline: String,
        val content: String,
        val author: User,
        val addedAt: String,
        val category: Category?,
        val tags: Set<Tag>,
        val views: Int,
        val likes: Int,
    )

    data class RenderedComment(val author: User, val content: String, val addedAt: String)
}
