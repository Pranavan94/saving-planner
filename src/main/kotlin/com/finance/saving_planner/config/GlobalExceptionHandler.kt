package com.finance.saving_planner.config

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {
    companion object {
        private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    data class ApiError(
        val timestamp: LocalDateTime = LocalDateTime.now(),
        val status: Int,
        val error: String,
        val message: String,
        val path: String
    )

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.warn("Malformed JSON request for {} {}", request.method, request.requestURI, ex)
        val message = when (val rootCause = ex.mostSpecificCause) {
            is InvalidFormatException -> {
                val field = rootCause.path.joinToString(".") { it.fieldName ?: "[index]" }
                "Invalid value for field '$field': ${rootCause.value}"
            }
            is MismatchedInputException -> {
                val field = rootCause.path.joinToString(".") { it.fieldName ?: "[index]" }
                if (field.isBlank()) "Malformed JSON request body"
                else "Missing or invalid field: '$field'"
            }
            else -> "Malformed JSON request body"
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiError(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = message,
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.warn("Argument type mismatch for {} {}", request.method, request.requestURI, ex)
        val paramName = ex.name
        val requiredType = ex.requiredType?.simpleName ?: "required type"
        val providedValue = ex.value?.toString() ?: "null"

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiError(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = "Invalid value '$providedValue' for parameter '$paramName'. Expected: $requiredType",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.warn("Validation failed for {} {}", request.method, request.requestURI, ex)
        val firstError = ex.bindingResult.fieldErrors.firstOrNull()
        val message = if (firstError != null) {
            "Validation failed for '${firstError.field}': ${firstError.defaultMessage}"
        } else {
            "Validation failed for request body"
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiError(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = message,
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingRequestParameter(
        ex: MissingServletRequestParameterException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.warn("Missing request parameter for {} {}", request.method, request.requestURI, ex)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiError(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = "Missing required parameter '${ex.parameterName}'",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.warn("Illegal argument for {} {}: {}", request.method, request.requestURI, ex.message, ex)
        val status = if (ex.message?.contains("not found", ignoreCase = true) == true) {
            HttpStatus.NOT_FOUND
        } else {
            HttpStatus.BAD_REQUEST
        }

        return ResponseEntity.status(status).body(
            ApiError(
                status = status.value(),
                error = status.reasonPhrase,
                message = ex.message ?: "Invalid request",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(
        ex: EntityNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.warn("Entity not found for {} {}: {}", request.method, request.requestURI, ex.message, ex)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiError(
                status = HttpStatus.NOT_FOUND.value(),
                error = HttpStatus.NOT_FOUND.reasonPhrase,
                message = ex.message ?: "Resource not found",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.error("Unhandled exception for {} {}", request.method, request.requestURI, ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiError(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                message = "Unexpected error occurred",
                path = request.requestURI
            )
        )
    }
}