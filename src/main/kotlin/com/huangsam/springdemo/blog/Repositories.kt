package com.huangsam.springdemo.blog

import java.time.LocalDateTime
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.CrudRepository

public interface ArticleRepository : CrudRepository<Article, Long> {
    // Fetch single article by URL slug and status (e.g., published articles for public viewing).
    // Used by: article detail page, RSS feed, public API endpoints.
    // EntityGraph eagerly loads author, category, tags in single query (prevents 3 separate
    // selects).
    // Status filter ensures readers can only see published articles (drafts hidden except to
    // author).
    @EntityGraph(attributePaths = ["author", "category", "tags"])
    public fun findBySlugAndStatus(slug: String, status: ArticleStatus): Article?

    // Fetch single article by slug without status filtering (loads both draft and published).
    // Used by: authenticated endpoints where author can see their own drafts.
    // Access control is checked in the controller (article.author == currentUser).
    @EntityGraph(attributePaths = ["author", "category", "tags"])
    public fun findBySlug(slug: String): Article?

    // Fetch paginated list of articles by status, ordered newest first.
    // Used by: home page article listing, public API with pagination (20 per page).
    // EntityGraph prevents N+1 when rendering author name and tags in each list item.
    // Latest articles shown first encourages readers to see recent content.
    @EntityGraph(attributePaths = ["author", "category", "tags"])
    public fun findAllByStatusOrderByAddedAtDesc(
        status: ArticleStatus,
        pageable: org.springframework.data.domain.Pageable,
    ): org.springframework.data.domain.Page<Article>

    // Fetch all articles (draft + published) paginated, newest first.
    // Used by: admin dashboard, article management interface.
    // No status filter allows admins to see articles in all states.
    @EntityGraph(attributePaths = ["author", "category", "tags"])
    public fun findAllByOrderByAddedAtDesc(
        pageable: org.springframework.data.domain.Pageable
    ): org.springframework.data.domain.Page<Article>

    // Fetch articles filtered by specific category and status, newest first.
    // Used by: category browse page (e.g., /category/spring-framework).
    // Status filter ensures only published articles appear in public category views.
    // EntityGraph prevents N+1 when displaying author and tags for each article.
    @EntityGraph(attributePaths = ["author", "category", "tags"])
    public fun findAllByCategoryAndStatusOrderByAddedAtDesc(
        category: Category,
        status: ArticleStatus,
    ): Iterable<Article>

    // Fetch articles containing a specific tag and published status, newest first.
    // Used by: tag browse pages (e.g., /tag/kotlin) for multi-dimensional discovery.
    // Status filter ensures only published articles visible in public tag views.
    // EntityGraph prevents N+1 when rendering article list with author/tags.
    @EntityGraph(attributePaths = ["author", "category", "tags"])
    public fun findAllByTagsContainingAndStatusOrderByAddedAtDesc(
        tag: Tag,
        status: ArticleStatus,
    ): Iterable<Article>

    // Fetch up to 5 most recent published articles by a specific author.
    // Used by: public user profile page showing author's published work.
    // Status filter ensures only published articles appear (no draft leaks).
    // Top 5 limit keeps profile pages fast and focused on best/recent content.
    @EntityGraph(attributePaths = ["author", "category", "tags"])
    public fun findTop5ByAuthorAndStatusOrderByAddedAtDesc(
        author: User,
        status: ArticleStatus,
    ): Iterable<Article>

    // Fetch up to 5 most recent articles by author (draft + published).
    // Used by: author's own dashboard showing their work in all states for editing.
    // No status filter allows author to see their draft articles.
    // Complements findTop5ByAuthorAndStatus for different access levels.
    @EntityGraph(attributePaths = ["author", "category", "tags"])
    public fun findTop5ByAuthorOrderByAddedAtDesc(author: User): Iterable<Article>

    // Fetch all draft articles scheduled for publishing before the given timestamp.
    // Used by: PublishingService scheduled task (runs every minute).
    // Looks for DRAFT articles where scheduledAt has passed, then auto-publishes them.
    // Critical for scheduled publishing feature (articles auto-go-live at specified time).
    @EntityGraph(attributePaths = ["author", "category", "tags"])
    public fun findAllByStatusAndScheduledAtBefore(
        status: ArticleStatus,
        now: LocalDateTime,
    ): List<Article>

    // Search articles by title substring (case-insensitive).
    // Used by: search feature /api/search endpoint.
    // This is a simple SQL LIKE search; more sophisticated search uses SearchService.
    // EntityGraph prevents N+1 when displaying search results with author/tags.
    @EntityGraph(attributePaths = ["author", "category", "tags"])
    public fun findAllByTitleContainingIgnoreCase(title: String): Iterable<Article>

    // Fetch all articles without pagination (draft + published).
    // Used by: SearchService for indexing, admin exports, full rebuilds.
    // EntityGraph prevents N+1 when iterating all articles to build search index.
    @EntityGraph(attributePaths = ["author", "category", "tags"])
    public override fun findAll(): List<Article>
}

public interface UserRepository : CrudRepository<User, Long> {
    // Fetch user by login/username for authentication.
    // Used by: Spring Security UserDetailsService during login, API endpoints validating user
    // exists.
    // Login is typically unique, so returns at most one user.
    public fun findByLogin(login: String): User?

    // Search users by login substring (case-insensitive).
    // Used by: admin user search, user directory features.
    public fun findAllByLoginContainingIgnoreCase(login: String): Iterable<User>

    // Fetch all users with a specific role ("USER" or "ADMIN").
    // Used by: admin management pages to filter/manage users by role.
    public fun findAllByRole(role: String): Iterable<User>

    // Fetch users by firstname, sorted by lastname alphabetically.
    // Used by: user directory/search features, organizational reports.
    public fun findAllByFirstnameOrderByLastnameAsc(firstname: String): Iterable<User>
}

public interface CommentRepository : CrudRepository<Comment, Long> {
    // Fetch all comments on an article, newest first.
    // Used by: article detail page to display comment thread.
    // EntityGraph eagerly loads author to prevent N+1 (one query per comment would be slow).
    // Ordered newest-last so most recent discussion appears at end of page.
    @EntityGraph(attributePaths = ["author"])
    public fun findAllByArticleOrderByAddedAtDesc(article: Article): Iterable<Comment>

    // Search comments by text content (case-insensitive).
    // Used by: admin moderation/search, monitoring for problematic comments.
    // EntityGraph loads both author and article context for search results.
    @EntityGraph(attributePaths = ["author", "article"])
    public fun findAllByContentContainingIgnoreCase(content: String): Iterable<Comment>
}

public interface CategoryRepository : CrudRepository<Category, Long> {
    // Fetch category by URL slug (e.g., "spring-framework").
    // Used by: /category/{slug} route to load articles in that category.
    public fun findBySlug(slug: String): Category?

    // Fetch category by name (used for find-or-create pattern).
    // Used by: article creation/editing to resolve category names to entities.
    public fun findByName(name: String): Category?

    // Fetch all categories alphabetically (cached).
    // Used by: navigation dropdowns, category selection in create/edit forms.
    // @Cacheable caches result since categories rarely change; stale list is acceptable.
    @Cacheable("categories") public fun findAllByOrderByNameAsc(): Iterable<Category>
}

public interface TagRepository : CrudRepository<Tag, Long> {
    // Fetch tag by URL slug (e.g., "kotlin").
    // Used by: /tag/{slug} route to load articles with that tag.
    public fun findBySlug(slug: String): Tag?

    // Fetch tag by name (used for find-or-create pattern).
    // Used by: article creation when resolving tag strings to entities.
    public fun findByName(name: String): Tag?

    // Batch fetch tags by names (IN clause).
    // Used by: article creation to resolve multiple tag names in a single query.
    // More efficient than N lookups when article has many tags.
    public fun findByNameIn(names: Collection<String>): Iterable<Tag>

    // Fetch all tags alphabetically (cached).
    // Used by: tag cloud in navigation, tag selection dropdowns in article creation.
    // @Cacheable caches since tags rarely change; stale list acceptable for UI.
    @Cacheable("tags") public fun findAllByOrderByNameAsc(): Iterable<Tag>
}
