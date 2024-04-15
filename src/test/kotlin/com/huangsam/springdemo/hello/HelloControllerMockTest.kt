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

private const val MOCK_MESSAGE = "Hello. Are you $DEFAULT_SAM?"

@SpringBootTest
@AutoConfigureMockMvc
class HelloControllerMockTest(
    @Autowired private val mockMvc: MockMvc
) {
    @MockBean
    private lateinit var helloService: HelloService

    @Test
    fun shouldReturnDefaultMessage() {
        `when`(helloService.greet(DEFAULT_SAM)).thenReturn(MOCK_MESSAGE)
        mockMvc.perform(get(HELLO_URL))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(
                content().string(containsString(MOCK_MESSAGE))
            )
    }
}
