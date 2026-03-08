package com.huangsam.springdemo.blog

import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.CrudRepository

interface ArticleRepository : CrudRepository<Article, Long> {
    // Single-entity fetch join so the author, category, and tags are available
    // without additional queries.
    @EntityGraph(attributePaths = ["author", "category", "tags"])
    fun findBySlug(slug: String): Article?

    // When listing articles we always render the author name, category, and tags
    // in the UI/JSON, so fetch them in the same select to avoid N+1.
    @EntityGraph(attributePaths = ["author", "category", "tags"])
    fun findAllByOrderByAddedAtDesc(
        pageable: org.springframework.data.domain.Pageable
    ): org.springframework.data.domain.Page<Article>

    // Filter articles by category, eagerly fetching related entities.
    @EntityGraph(attributePaths = ["author", "category", "tags"])
    fun findAllByCategoryOrderByAddedAtDesc(category: Category): Iterable<Article>

    // Filter articles that contain a specific tag.
    @EntityGraph(attributePaths = ["author", "category", "tags"])
    fun findAllByTagsContainingOrderByAddedAtDesc(tag: Tag): Iterable<Article>

    // Profile page: fetch recent articles written by a specific user.
    @EntityGraph(attributePaths = ["author", "category", "tags"])
    fun findTop5ByAuthorOrderByAddedAtDesc(author: User): Iterable<Article>

    @EntityGraph(attributePaths = ["author", "category", "tags"])
    override fun findAll(): List<Article>
}

interface UserRepository : CrudRepository<User, Long> {
    fun findByLogin(login: String): User?

    fun findAllByFirstnameOrderByLastnameAsc(firstname: String): Iterable<User>
}

interface CommentRepository : CrudRepository<Comment, Long> {
    // Callers always show comment author, so load it eagerly to avoid a
    // separate query per comment.
    @EntityGraph(attributePaths = ["author"])
    fun findAllByArticleOrderByAddedAtDesc(article: Article): Iterable<Comment>
}

interface CategoryRepository : CrudRepository<Category, Long> {
    fun findBySlug(slug: String): Category?

    fun findByName(name: String): Category?

    @Cacheable("categories") fun findAllByOrderByNameAsc(): Iterable<Category>
}

interface TagRepository : CrudRepository<Tag, Long> {
    fun findBySlug(slug: String): Tag?

    fun findByName(name: String): Tag?

    fun findByNameIn(names: Collection<String>): Iterable<Tag>

    @Cacheable("tags") fun findAllByOrderByNameAsc(): Iterable<Tag>
}
