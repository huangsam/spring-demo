package com.huangsam.springdemo.blog

import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

@WebMvcTest(AdminController::class)
@Import(SecurityConfiguration::class)
internal class AdminControllerTests
@Autowired
constructor(
    private val mockMvc: MockMvc,
    @MockitoBean private val articleRepository: ArticleRepository,
    @MockitoBean private val userRepository: UserRepository,
    @MockitoBean private val commentRepository: CommentRepository,
    @MockitoBean private val categoryRepository: CategoryRepository,
) {

    @Test
    internal fun `Access dashboard as USER is forbidden`() {
        mockMvc
            .perform(get("/admin").with(user("johnDoe").roles("USER")))
            .andExpect(status().isForbidden)
    }

    @Test
    internal fun `Access dashboard as ADMIN is permitted`() {
        val user = User("admin", "Admin", "User", role = "ADMIN", password = "password")
        `when`(userRepository.findByLogin("admin")).thenReturn(user)
        `when`(categoryRepository.findAll()).thenReturn(emptyList())
        `when`(commentRepository.findAll()).thenReturn(emptyList())

        mockMvc
            .perform(get("/admin").with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk)
            .andExpect(view().name("admin-dashboard"))
            .andExpect(model().attributeExists("articlesCount", "usersCount", "commentsCount"))
            .andExpect(model().attribute("isAdmin", true))
    }

    @Test
    internal fun `Delete article and redirect`() {
        mockMvc
            .perform(
                post("/admin/articles/delete/1").with(user("admin").roles("ADMIN")).with(csrf())
            )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/admin/articles"))
    }

    @Test
    internal fun `Delete user and redirect`() {
        mockMvc
            .perform(post("/admin/users/delete/1").with(user("admin").roles("ADMIN")).with(csrf()))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/admin/users"))
    }

    @Test
    internal fun `Delete comment and redirect`() {
        mockMvc
            .perform(
                post("/admin/comments/delete/1").with(user("admin").roles("ADMIN")).with(csrf())
            )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/admin/comments"))
    }

    @Test
    internal fun `Filter articles by title`() {
        val author = User("login", "first", "last", password = "password", id = 1L)
        val article = Article("title", "headline", "content", author, Category("cat"), id = 1L)
        `when`(articleRepository.findAllByTitleContainingIgnoreCase("test"))
            .thenReturn(listOf(article))

        mockMvc
            .perform(
                get("/admin/articles").param("title", "test").with(user("admin").roles("ADMIN"))
            )
            .andExpect(status().isOk)
            .andExpect(model().attributeExists("articles"))
            .andExpect(model().attribute("filterTitle", "test"))
    }

    @Test
    internal fun `Filter users by login`() {
        val user = User("login", "first", "last", password = "password", id = 1L)
        `when`(userRepository.findAllByLoginContainingIgnoreCase("test")).thenReturn(listOf(user))

        mockMvc
            .perform(get("/admin/users").param("login", "test").with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk)
            .andExpect(model().attributeExists("users"))
            .andExpect(model().attribute("filterLogin", "test"))
    }

    @Test
    internal fun `Filter comments by content`() {
        val author = User("login", "first", "last", password = "password", id = 1L)
        val article = Article("title", "headline", "c", author, Category("c"), id = 1L)
        val comment = Comment(article, author, "content", id = 1L)
        `when`(commentRepository.findAllByContentContainingIgnoreCase("test"))
            .thenReturn(listOf(comment))

        mockMvc
            .perform(
                get("/admin/comments").param("content", "test").with(user("admin").roles("ADMIN"))
            )
            .andExpect(status().isOk)
            .andExpect(model().attributeExists("comments"))
            .andExpect(model().attribute("filterContent", "test"))
    }
}
