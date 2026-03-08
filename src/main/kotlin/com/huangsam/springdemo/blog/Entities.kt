package com.huangsam.springdemo.blog

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

enum class ArticleStatus {
    DRAFT,
    PUBLISHED,
}

@Entity
// Blog article. Articles can be DRAFT (private, only visible to author) or PUBLISHED (public).
// All relationships use lazy-loading by default, requiring explicit eager fetching in queries
// via @EntityGraph to prevent N+1 query problems.
class Article(
    // Display title shown in lists and article header
    var title: String,
    // Short summary/headline for article previews
    var headline: String,
    // Full article content (up to 10,000 characters); typically markdown-formatted
    @Column(length = 10000) var content: String,
    // Author of the article (required). Lazily-loaded; explicitly fetched in repository queries.
    @ManyToOne var author: User,
    // Single category classification (optional). Lazily-loaded; explicitly fetched in queries.
    // Articles can be browsed by category for navigation/discovery.
    @ManyToOne var category: Category? = null,
    // Multiple tags for flexible labeling (optional). Lazily-loaded; explicitly fetched in queries.
    // Unlike category (hierarchical), tags are lightweight labels (many per article).
    @ManyToMany var tags: MutableSet<Tag> = mutableSetOf(),
    // URL-friendly identifier derived from title for SEO-friendly URLs (e.g., "my-first-article").
    // Automatically generated from title but can be overridden by callers.
    var slug: String = title.toSlug(),
    // When the article was created. Defaults to now; used for sorting in "newest first" views.
    var addedAt: LocalDateTime = LocalDateTime.now(),
    // Number of times this article has been viewed (for tracking popularity)
    var views: Int = 0,
    // User-given likes count (simple engagement metric)
    var likes: Int = 0,
    // DRAFT = private, only visible to author; PUBLISHED = public to all
    var status: ArticleStatus = ArticleStatus.DRAFT,
    // When to automatically publish this article (null = publish immediately when saving).
    // PublishingService checks this every minute and auto-publishes when the time arrives.
    var scheduledAt: LocalDateTime? = null,
    // Database primary key (auto-generated)
    @Id @GeneratedValue var id: Long? = null,
)

@Entity
// Application user with authentication credentials and role-based authorization.
// Passwords are encrypted using BCrypt before storage.
class User(
    // Login/username for authentication (must be unique in the application)
    var login: String,
    // User's first name (for display on profile page and in article bylines)
    var firstname: String,
    // User's last name (for display on profile page and in article bylines)
    var lastname: String,
    // Optional biography shown on user profile page
    var description: String? = null,
    // User's password stored as BCrypt hash (never plain text). Hashed during registration.
    var password: String,
    // Simple role for authorization: "USER" (can write articles/comments) or "ADMIN" (can manage
    // site).
    // Note: In a real app, use a proper Role/Authority enum or join table for more complex RBAC.
    var role: String = "USER",
    // Database primary key (auto-generated)
    @Id @GeneratedValue var id: Long? = null,
)

@Entity
// Reader comment on an article. Comments are only visible after the article is PUBLISHED.
// Both relationships are lazy-loaded; queries that fetch comments explicitly load the author
// via @EntityGraph to avoid N+1 problems (every comment needs to show its author's name).
class Comment(
    // The article being commented on (required). Lazily-loaded.
    @ManyToOne var article: Article,
    // The user who wrote this comment (required). Lazily-loaded; explicitly fetched in queries.
    // Comment visibility is tied to the commenter's identity for authorization.
    @ManyToOne var author: User,
    // The comment text (up to 2,000 characters)
    @Column(length = 2000) var content: String,
    // When the comment was posted. Used for sorting comments chronologically.
    var addedAt: LocalDateTime = LocalDateTime.now(),
    // Database primary key (auto-generated)
    @Id @GeneratedValue var id: Long? = null,
)

@Entity
// Hierarchical classification for articles (e.g., "Technology", "Travel").
// Articles have exactly zero-or-one category; categories are cached since they change rarely.
// Unlike tags, categories are intentionally limited (for navigation hierarchy).
class Category(
    // Category display name (e.g., "Spring Framework", "Database Design")
    var name: String,
    // URL-friendly slug derived from name (e.g., "spring-framework"). Used in /category/{slug}
    // routes.
    var slug: String = name.toSlug(),
    // Database primary key (auto-generated)
    @Id @GeneratedValue var id: Long? = null,
)

@Entity
// Lightweight, flexible labels for articles (e.g., "kotlin", "performance", "tutorial").
// Unlike categories, articles can have many tags, and tags are cached since they change rarely.
// Tags enable discovery along multiple dimensions (browse by /tag/{slug}).
class Tag(
    // Tag display name (e.g., "Kotlin", "Spring Security", "Testing")
    var name: String,
    // URL-friendly slug derived from name (e.g., "spring-security"). Used in /tag/{slug} routes.
    var slug: String = name.toSlug(),
    // Database primary key (auto-generated)
    @Id @GeneratedValue var id: Long? = null,
)
