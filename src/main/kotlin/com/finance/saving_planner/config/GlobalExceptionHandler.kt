package com.finance.saving_planner.config

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.owasp.encoder.Encode
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

    private fun sanitize(value: Any?): String = Encode.forHtml(value?.toString().orEmpty())

    private fun errorResponse(
        status: HttpStatus,
        message: String,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        return ResponseEntity.status(status).body(
            ApiError(
                status = status.value(),
                error = status.reasonPhrase,
                message = message,
                path = sanitize(request.requestURI)
            )
        )
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
                val field = rootCause.path.joinToString(".") { sanitize(it.fieldName ?: "[index]") }
                val invalidValue = sanitize(rootCause.value)
                "Invalid value for field '$field': $invalidValue"
            }
            is MismatchedInputException -> {
                val field = rootCause.path.joinToString(".") { sanitize(it.fieldName ?: "[index]") }
                if (field.isBlank()) "Malformed JSON request body"
                else "Missing or invalid field: '$field'"
            }
            else -> "Malformed JSON request body"
        }

        return errorResponse(HttpStatus.BAD_REQUEST, message, request)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.warn("Argument type mismatch for {} {}", request.method, request.requestURI, ex)
        val paramName = sanitize(ex.name)
        val requiredType = sanitize(ex.requiredType?.simpleName ?: "required type")
        val providedValue = sanitize(ex.value ?: "null")

        return errorResponse(
            HttpStatus.BAD_REQUEST,
            "Invalid value '$providedValue' for parameter '$paramName'. Expected: $requiredType",
            request
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
            val field = sanitize(firstError.field)
            val errorMessage = sanitize(firstError.defaultMessage ?: "Invalid value")
            "Validation failed for '$field': $errorMessage"
        } else {
            "Validation failed for request body"
        }

        return errorResponse(HttpStatus.BAD_REQUEST, message, request)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingRequestParameter(
        ex: MissingServletRequestParameterException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.warn("Missing request parameter for {} {}", request.method, request.requestURI, ex)
        val parameterName = sanitize(ex.parameterName)
        return errorResponse(
            HttpStatus.BAD_REQUEST,
            "Missing required parameter '$parameterName'",
            request
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.warn("Illegal argument for {} {}: {}", request.method, request.requestURI, ex.message, ex)
        val rawMessage = ex.message ?: "Invalid request"
        val status = if (rawMessage.contains("not found", ignoreCase = true)) {
            HttpStatus.NOT_FOUND
        } else {
            HttpStatus.BAD_REQUEST
        }

        return errorResponse(status, sanitize(rawMessage), request)
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(
        ex: EntityNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.warn("Entity not found for {} {}: {}", request.method, request.requestURI, ex.message, ex)
        return errorResponse(
            HttpStatus.NOT_FOUND,
            sanitize(ex.message ?: "Resource not found"),
            request
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.error("Unhandled exception for {} {}", request.method, request.requestURI, ex)
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred", request)
    }
}