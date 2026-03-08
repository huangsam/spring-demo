package com.huangsam.springdemo.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class RateLimitInterceptor : HandlerInterceptor {

    private val buckets: ConcurrentHashMap<String, Bucket> = ConcurrentHashMap()

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val clientId = getClientIp(request)
        val bucket = buckets.computeIfAbsent(clientId) { createNewBucket() }

        return if (bucket.tryConsume(1)) {
            true
        } else {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.writer.write("Too many requests from $clientId. Please try again later.")
            false
        }
    }

    private fun createNewBucket(): Bucket {
        val limit =
            Bandwidth.builder().capacity(100).refillGreedy(100, Duration.ofMinutes(1)).build()
        return Bucket.builder().addLimit(limit).build()
    }

    private fun getClientIp(request: HttpServletRequest): String {
        val xfHeader = request.getHeader("X-Forwarded-For")
        return if (xfHeader == null) {
            request.remoteAddr
        } else {
            xfHeader.split(",").firstOrNull()?.trim() ?: request.remoteAddr
        }
    }
}
