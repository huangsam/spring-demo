package com.huangsam.springdemo.blog

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.CrudRepository

interface ArticleRepository : CrudRepository<Article, Long> {
    // Single-entity fetch join so the author is available without a second query.
    // Applying an EntityGraph on the base method keeps things simple; callers
    // can just use `findBySlug` and still get the author eagerly fetched.
    @EntityGraph(attributePaths = ["author"]) fun findBySlug(slug: String): Article?

    // When listing articles we always render the author name in the UI/JSON,
    // so fetch it in the same select to avoid N+1.
    @EntityGraph(attributePaths = ["author"]) fun findAllByOrderByAddedAtDesc(): Iterable<Article>
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
