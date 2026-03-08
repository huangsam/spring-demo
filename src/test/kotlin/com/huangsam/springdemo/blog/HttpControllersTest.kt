package com.huangsam.springdemo.blog

import com.fasterxml.jackson.databind.ObjectMapper
import com.huangsam.springdemo.Routes
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(
    controllers = [ArticleController::class, UserController::class, CommentController::class]
)
class HttpControllersTest
@Autowired
constructor(
    private val mockMvc: MockMvc,
    @MockitoBean private val articleRepository: ArticleRepository,
    @MockitoBean private val userRepository: UserRepository,
    @MockitoBean private val commentRepository: CommentRepository,
    @MockitoBean private val categoryRepository: CategoryRepository,
    @MockitoBean private val tagRepository: TagRepository,
    @MockitoBean
    private val passwordEncoder: org.springframework.security.crypto.password.PasswordEncoder,
) {
    private val objectMapper = ObjectMapper()

    @Test
    fun `List articles`() {
        val johnDoe = User("johnDoe", "John", "Doe", password = "password")
        val category = Category("Frameworks")
        val tag = Tag("Spring")
        val lorem5Article =
            Article(
                "Lorem",
                "Lorem",
                "dolor sit amet",
                johnDoe,
                category = category,
                tags = mutableSetOf(tag),
            )
        val ipsumArticle = Article("Ipsum", "Ipsum", "dolor sit amet", johnDoe)
        `when`(articleRepository.findAllByOrderByAddedAtDesc())
            .thenReturn(listOf(lorem5Article, ipsumArticle))
        mockMvc
            .perform(get("${Routes.API_ARTICLE}/").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("\$.[0].author.login").value(johnDoe.login))
            .andExpect(jsonPath("\$.[0].slug").value(lorem5Article.slug))
            .andExpect(jsonPath("\$.[0].category.name").value("Frameworks"))
            .andExpect(jsonPath("\$.[0].tags[0].name").value("Spring"))
            .andExpect(jsonPath("\$.[1].author.login").value(johnDoe.login))
            .andExpect(jsonPath("\$.[1].slug").value(ipsumArticle.slug))
    }

    @Test
    fun `Get article by slug`() {
        val johnDoe = User("johnDoe", "John", "Doe", password = "password")
        val article = Article("Lorem", "Lorem", "dolor sit amet", johnDoe)
        `when`(articleRepository.findBySlug(article.slug)).thenReturn(article)
        mockMvc
            .perform(
                get("${Routes.API_ARTICLE}/${article.slug}").accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("\$.slug").value(article.slug))
            .andExpect(jsonPath("\$.author.login").value(johnDoe.login))
    }

    @Test
    fun `Get article by slug returns 404 when not found`() {
        `when`(articleRepository.findBySlug("nonexistent")).thenReturn(null)
        mockMvc
            .perform(get("${Routes.API_ARTICLE}/nonexistent").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(username = "johnDoe", roles = ["USER"])
    fun `Create article returns created article`() {
        val request =
            ArticleRequest(
                "New Title",
                "New Headline",
                "New Content",
                "Frameworks",
                listOf("Spring", "Kotlin"),
            )
        val author = User("johnDoe", "John", "Doe", password = "password")
        val category = Category("Frameworks")
        val tag1 = Tag("Spring")
        val tag2 = Tag("Kotlin")
        val savedArticle =
            Article(
                "New Title",
                "New Headline",
                "New Content",
                author,
                category = category,
                tags = mutableSetOf(tag1, tag2),
            )

        `when`(userRepository.findByLogin("johnDoe")).thenReturn(author)
        `when`(categoryRepository.findByName("Frameworks")).thenReturn(category)
        `when`(tagRepository.findByName("Spring")).thenReturn(tag1)
        `when`(tagRepository.findByName("Kotlin")).thenReturn(tag2)
        `when`(articleRepository.save(org.mockito.ArgumentMatchers.any(Article::class.java)))
            .thenReturn(savedArticle)

        mockMvc
            .perform(
                post("${Routes.API_ARTICLE}/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("\$.title").value("New Title"))
            .andExpect(jsonPath("\$.category.name").value("Frameworks"))
            .andExpect(jsonPath("\$.tags[0].name").value("Spring"))
    }

    @Test
    fun `List users`() {
        val johnDoe = User("johnDoe", "John", "Doe", password = "password")
        val janeDoe = User("janeDoe", "Jane", "Doe", password = "password")
        `when`(userRepository.findAll()).thenReturn(listOf(johnDoe, janeDoe))
        mockMvc
            .perform(get("${Routes.API_USER}/").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("\$.[0].login").value(johnDoe.login))
            .andExpect(jsonPath("\$.[1].login").value(janeDoe.login))
    }

    @Test
    fun `Get user by login`() {
        val johnDoe = User("johnDoe", "John", "Doe", password = "password")
        `when`(userRepository.findByLogin(johnDoe.login)).thenReturn(johnDoe)
        mockMvc
            .perform(get("${Routes.API_USER}/${johnDoe.login}").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("\$.login").value(johnDoe.login))
    }

    @Test
    fun `Get user by login returns 404 when not found`() {
        `when`(userRepository.findByLogin("unknown")).thenReturn(null)
        mockMvc
            .perform(get("${Routes.API_USER}/unknown").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Register user returns created user`() {
        val request = RegistrationRequest("newUser", "New", "User", "secret")
        val savedUser =
            User(request.login, request.firstname, request.lastname, password = "encoded")
        `when`(userRepository.findByLogin(request.login)).thenReturn(null)
        `when`(passwordEncoder.encode(request.password)).thenReturn("encoded")
        `when`(userRepository.save(org.mockito.ArgumentMatchers.any(User::class.java)))
            .thenReturn(savedUser)
        mockMvc
            .perform(
                post("${Routes.API_USER}/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("\$.login").value(request.login))
    }

    @Test
    fun `Register user returns 400 when login already exists`() {
        val request = RegistrationRequest("existingUser", "Existing", "User", "secret")
        val existingUser =
            User(request.login, request.firstname, request.lastname, password = "encoded")
        `when`(userRepository.findByLogin(request.login)).thenReturn(existingUser)
        mockMvc
            .perform(
                post("${Routes.API_USER}/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `Get comments by article slug`() {
        val johnDoe = User("johnDoe", "John", "Doe", password = "password")
        val article = Article("Lorem", "Lorem", "dolor sit amet", johnDoe)
        val comment = Comment(article, johnDoe, "Nice post!")
        `when`(articleRepository.findBySlug(article.slug)).thenReturn(article)
        `when`(commentRepository.findAllByArticleOrderByAddedAtDesc(article))
            .thenReturn(listOf(comment))
        mockMvc
            .perform(
                get("${Routes.API_COMMENT}/${article.slug}").accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("\$.[0].content").value(comment.content))
    }

    @Test
    fun `Get comments returns 404 when article not found`() {
        `when`(articleRepository.findBySlug("nonexistent")).thenReturn(null)
        mockMvc
            .perform(get("${Routes.API_COMMENT}/nonexistent").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }
}
