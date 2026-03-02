package com.huangsam.springdemo.blog

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class Article(
    var title: String,
    var headline: String,
    @Column(length = 10000) var content: String,
    @ManyToOne var author: User,
    var slug: String = title.toSlug(),
    var addedAt: LocalDateTime = LocalDateTime.now(),
    @Id @GeneratedValue var id: Long? = null
)

@Entity
class User(
    var login: String,
    var firstname: String,
    var lastname: String,
    var description: String? = null,
    // Password stored as-is for now, can explore robust mechanisms later
    var password: String = "password",
    var role: String = "USER",
    @Id @GeneratedValue var id: Long? = null
)

@Entity
class Comment(
    @ManyToOne var article: Article,
    @ManyToOne var author: User,
    @Column(length = 2000) var content: String,
    var addedAt: LocalDateTime = LocalDateTime.now(),
    @Id @GeneratedValue var id: Long? = null
)
