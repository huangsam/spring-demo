package com.huangsam.springdemo.hello

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class HelloControllerMockTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var helloService: HelloService

    private val mockMessage = "Hello. Are you $DEFAULT_SAM?"

    @Test
    fun shouldReturnDefaultMessage() {
        `when`(helloService.greet(DEFAULT_SAM)).thenReturn(mockMessage)

        mockMvc.perform(get(HELLO_URL))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(
                content().string(containsString(mockMessage))
            )
    }
}
