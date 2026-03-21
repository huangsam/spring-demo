package com.huangsam.springdemo.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [TestController::class])
@Import(WebConfig::class, RateLimitInterceptor::class)
internal class RateLimitInterceptorTest {

    @Autowired private lateinit var mockMvc: MockMvc

    @Test
    internal fun `Rate limiting allows first 100 requests but blocks the 101st`() {
        val clientId = "192.168.1.1"

        // Make 100 allowed requests
        (1..100).forEach { _ ->
            mockMvc
                .perform(get("/api/test-rate-limit").header("X-Forwarded-For", clientId))
                .andExpect(status().isOk)
        }

        // The 101st request should be blocked
        mockMvc
            .perform(get("/api/test-rate-limit").header("X-Forwarded-For", clientId))
            .andExpect(status().isTooManyRequests)

        // A different IP should have a fresh bucket
        val newClientId = "192.168.1.2"
        mockMvc
            .perform(get("/api/test-rate-limit").header("X-Forwarded-For", newClientId))
            .andExpect(status().isOk)
    }
}

// Dummy controller for testing
@org.springframework.web.bind.annotation.RestController
internal class TestController {
    @org.springframework.web.bind.annotation.GetMapping("/api/test-rate-limit")
    internal fun testEndpoint(): String {
        return "OK"
    }
}
