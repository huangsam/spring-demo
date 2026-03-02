package com.huangsam.springdemo.blog

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface ArticleRepository : CrudRepository<Article, Long> {
    // Single-entity fetch join so the author is available without a second query
    @Query("select a from Article a join fetch a.author where a.slug = :slug")
    fun findBySlugWithAuthor(slug: String): Article?

    fun findBySlug(slug: String): Article?
    fun findAllByOrderByAddedAtDesc(): Iterable<Article>

    // Explicit fetch join avoids N+1 when iterating and touching the author
    @Query("""
        select a from Article a
        join fetch a.author
        order by a.addedAt desc
    """)
    fun findAllWithAuthorOrderByAddedAtDesc(): List<Article>
}

interface UserRepository : CrudRepository<User, Long> {
    fun findByLogin(login: String): User?
    fun findAllByFirstnameOrderByLastnameAsc(firstname: String): Iterable<User>
}

interface CommentRepository : CrudRepository<Comment, Long> {
    fun findAllByArticleOrderByAddedAtDesc(article: Article): Iterable<Comment>

    // Explicit fetch join avoids N+1 when iterating and touching the author
    @Query("""
        select c from Comment c
        join fetch c.author
        where c.article = :article
        order by c.addedAt desc
    """)
    fun findAllByArticleWithAuthorOrderByAddedAtDesc(article: Article): List<Comment>
}
