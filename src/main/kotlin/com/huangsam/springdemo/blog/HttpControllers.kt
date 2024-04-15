package com.huangsam.springdemo.blog

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import kotlin.jvm.Throws

@RestController
@RequestMapping("/api/article")
class ArticleController(private val repository: ArticleRepository) {
    @GetMapping("/")
    fun findAll(): Iterable<Article> = repository.findAllByOrderByAddedAtDesc()

    @Throws(ResponseStatusException::class)
    @GetMapping("/{slug}")
    fun findOne(@PathVariable slug: String): Article =
        repository.findBySlug(slug)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This article does not exist")
}

@RestController
@RequestMapping("/api/user")
class UserController(private val repository: UserRepository) {
    @GetMapping("/")
    fun findAll(): Iterable<User> = repository.findAll()

    @Throws(ResponseStatusException::class)
    @GetMapping("/{login}")
    fun findOne(@PathVariable login: String): User =
        repository.findByLogin(login)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This user does not exist")
}
