package com.huangsam.springdemo.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

// GlobalExceptionHandler provides centralized error handling for validation errors.
// Catches MethodArgumentNotValidException and returns standardized JSON responses.
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    public fun handleValidationException(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {
        val errors =
            ex.bindingResult.fieldErrors.associate { error ->
                error.field to (error.defaultMessage ?: "Validation failed")
            }
        val errorResponse =
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                message = "Validation failed",
                errors = errors,
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
}

// ErrorResponse is a standardized format for API error responses.
public data class ErrorResponse(
    public val status: Int,
    public val message: String,
    public val errors: Map<String, String>? = null,
)
