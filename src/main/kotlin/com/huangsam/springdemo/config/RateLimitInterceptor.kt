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

// Rate limiting interceptor using the token bucket algorithm (via Bucket4j library).
// Implements per-client IP rate limiting to prevent abuse and DOS attacks.
// The bucket has a capacity of 100 tokens that refill greedily at 100 tokens/minute,
// meaning each client can make up to 100 requests per minute before being rate-limited.
@Component
public class RateLimitInterceptor : HandlerInterceptor {

    // Each client IP gets its own bucket; ConcurrentHashMap is thread-safe for multi-threaded
    // servlet access
    private val buckets: ConcurrentHashMap<String, Bucket> = ConcurrentHashMap()

    // Intercepts each HTTP request before it reaches the controller.
    // Returns true to allow the request to proceed, false to reject it (with a 429 response).
    public override fun preHandle(
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

    // Creates a new token bucket for a client with a 100-token capacity that refills
    // greedily at 100 tokens per minute. The greedy refill means we add all tokens
    // at the start of each minute window rather than slowly over time.
    private fun createNewBucket(): Bucket {
        val limit =
            Bandwidth.builder().capacity(100).refillGreedy(100, Duration.ofMinutes(1)).build()
        return Bucket.builder().addLimit(limit).build()
    }

    // Extracts the client IP address, checking X-Forwarded-For header first (for requests
    // behind a proxy or load balancer) before falling back to remoteAddr.
    private fun getClientIp(request: HttpServletRequest): String {
        val xfHeader = request.getHeader("X-Forwarded-For")
        return if (xfHeader == null) {
            request.remoteAddr
        } else {
            xfHeader.split(",").firstOrNull()?.trim() ?: request.remoteAddr
        }
    }
}
