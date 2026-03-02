package com.huangsam.springdemo.blog

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
// Simple blog article. Most associations use the default
// FetchType.LAZY, which is why we later needed to fetch the
// `author` explicitly in repository queries to avoid N+1 issues.
class Article(
    var title: String,
    var headline: String,
    @Column(length = 10000) var content: String,
    @ManyToOne var author: User,
    // Slug is derived from title by default; callers can override
    var slug: String = title.toSlug(),
    var addedAt: LocalDateTime = LocalDateTime.now(),
    @Id @GeneratedValue var id: Long? = null
)

@Entity
// Application user. Passwords are stored encrypted (BCrypt).
// `role` is a simple string since we only have USER/ADMIN in demo.
class User(
    var login: String,
    var firstname: String,
    var lastname: String,
    var description: String? = null,
    var password: String,
    var role: String = "USER",
    @Id @GeneratedValue var id: Long? = null
)

@Entity
// Comments are simple and link back to both the article and the author.
// The many-to-one associations are lazy by default; when loading a list of
// comments we intentionally fetch `author` to avoid N+1 selects.
class Comment(
    @ManyToOne var article: Article,
    @ManyToOne var author: User,
    @Column(length = 2000) var content: String,
    var addedAt: LocalDateTime = LocalDateTime.now(),
    @Id @GeneratedValue var id: Long? = null
)
