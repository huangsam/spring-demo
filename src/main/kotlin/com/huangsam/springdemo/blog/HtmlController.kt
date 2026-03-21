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
public class HtmlController(
    private val repository: ArticleRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
    private val markdownConverter: MarkdownConverter,
    private val searchService: SearchService,
) {
    @GetMapping(Routes.ROOT)
    public fun blog(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) search: String?,
        model: Model,
    ): String {
        model["title"] = if (search.isNullOrBlank()) "Blog" else "Search Results"
        model["searchQuery"] = search ?: ""

        if (!search.isNullOrBlank()) {
            val rankedIds = searchService.search(search)
            val articles = repository.findAllById(rankedIds).associateBy { it.id!! }
            val rankedArticles = rankedIds.mapNotNull { articles[it] }

            model["articles"] = rankedArticles.map { it.render() }
            model["isSearch"] = true
            model["hasResults"] = rankedArticles.isNotEmpty()
            model["totalResults"] = rankedArticles.size
        } else {
            val pageSize = 5
            val pageable = PageRequest.of(page, pageSize)
            val articlePage =
                repository.findAllByStatusOrderByAddedAtDesc(ArticleStatus.PUBLISHED, pageable)

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
            model["isSearch"] = false
        }

        val categories = categoryRepository.findAllByOrderByNameAsc().toList()
        model["categories"] = categories
        model["hasCategories"] = categories.isNotEmpty()

        val allTags = tagRepository.findAllByOrderByNameAsc().toList()
        model["allTags"] = allTags
        model["hasTags"] = allTags.isNotEmpty()
        val authenticatedUser = getAuthenticatedUser()
        model["user"] = authenticatedUser
        model["isAdmin"] = authenticatedUser?.role == "ADMIN"
        return MustacheView.BLOG
    }

    @GetMapping("/login")
    public fun login(model: Model): String {
        model["title"] = "Login"
        return "login"
    }

    @GetMapping("/register")
    public fun register(model: Model): String {
        model["title"] = "Register"
        return "register"
    }

    @GetMapping("/new-article")
    public fun newArticle(model: Model): String {
        val authenticatedUser = getAuthenticatedUser()
        model["title"] = "New Article"
        model["user"] = authenticatedUser
        model["isAdmin"] = authenticatedUser?.role == "ADMIN"
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
    public fun article(@PathVariable slug: String, model: Model): String {
        val article =
            repository.findBySlug(slug)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This article does not exist",
                )

        // Only allow published articles to be viewed publicly,
        // unless the viewer is the author.
        val currentUser = getAuthenticatedUser()
        if (article.status == ArticleStatus.DRAFT && article.author.login != currentUser?.login) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "This article is not yet published")
        }

        // Increment views and save
        article.views++
        repository.save(article)

        model["title"] = article.title
        model["article"] = article.render()
        model["comments"] =
            commentRepository.findAllByArticleOrderByAddedAtDesc(article).map { it.render() }

        val relatedIds = searchService.findRelated(article)
        val relatedArticles = repository.findAllById(relatedIds).associateBy { it.id!! }
        model["relatedArticles"] = relatedIds.mapNotNull { relatedArticles[it] }.map { it.render() }
        model["hasRelatedArticles"] = relatedIds.isNotEmpty()

        val authenticatedUser = getAuthenticatedUser()
        model["user"] = authenticatedUser
        model["isAdmin"] = authenticatedUser?.role == "ADMIN"
        return MustacheView.ARTICLE
    }

    @GetMapping("${Routes.CATEGORY}/{slug}")
    public fun category(@PathVariable slug: String, model: Model): String {
        val category =
            categoryRepository.findBySlug(slug)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This category does not exist",
                )
        model["title"] = "Category: ${category.name}"
        model["category"] = category
        model["articles"] =
            repository
                .findAllByCategoryAndStatusOrderByAddedAtDesc(category, ArticleStatus.PUBLISHED)
                .map { it.render() }
        val authenticatedUser = getAuthenticatedUser()
        model["user"] = authenticatedUser
        model["isAdmin"] = authenticatedUser?.role == "ADMIN"
        return MustacheView.CATEGORY
    }

    @GetMapping("${Routes.TAG}/{slug}")
    public fun tag(@PathVariable slug: String, model: Model): String {
        val tag =
            tagRepository.findBySlug(slug)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This tag does not exist")
        model["title"] = "Tag: ${tag.name}"
        model["tag"] = tag
        model["articles"] =
            repository
                .findAllByTagsContainingAndStatusOrderByAddedAtDesc(tag, ArticleStatus.PUBLISHED)
                .map { it.render() }
        val authenticatedUser = getAuthenticatedUser()
        model["user"] = authenticatedUser
        model["isAdmin"] = authenticatedUser?.role == "ADMIN"
        return MustacheView.TAG
    }

    @GetMapping("${Routes.USER}/{login}")
    public fun profile(@PathVariable login: String, model: Model): String {
        val profileUser =
            userRepository.findByLogin(login)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This user does not exist")
        model["title"] = "${profileUser.firstname} ${profileUser.lastname}"
        model["profileUser"] = profileUser

        val articles =
            if (profileUser.login == getAuthenticatedUser()?.login) {
                repository.findTop5ByAuthorOrderByAddedAtDesc(profileUser)
            } else {
                repository.findTop5ByAuthorAndStatusOrderByAddedAtDesc(
                    profileUser,
                    ArticleStatus.PUBLISHED,
                )
            }

        model["articles"] = articles.map { it.render() }
        val authenticatedUser = getAuthenticatedUser()
        model["user"] = authenticatedUser
        model["isAdmin"] = authenticatedUser?.role == "ADMIN"
        return MustacheView.PROFILE
    }

    public fun Article.render(): RenderedArticle =
        RenderedArticle(
            slug,
            title,
            headline,
            markdownConverter.convertToHtml(content),
            author,
            addedAt.format(),
            category,
            tags.sortedBy { it.name },
            views,
            likes,
        )

    public fun Comment.render(): RenderedComment =
        RenderedComment(author, content, addedAt.format())

    public data class RenderedArticle(
        public val slug: String,
        public val title: String,
        public val headline: String,
        public val content: String,
        public val author: User,
        public val addedAt: String,
        public val category: Category?,
        public val tags: List<Tag>,
        public val views: Int,
        public val likes: Int,
    )

    public data class RenderedComment(
        public val author: User,
        public val content: String,
        public val addedAt: String,
    )
}
