package com.finance.saving_planner.config

import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.bind.MissingServletRequestParameterException

@DisplayName("GlobalExceptionHandler output sanitization tests")
class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleIllegalArgumentException escapes reflected message and request path`() {
        val request = MockHttpServletRequest("GET", "/api/v1/users/<script>alert(1)</script>")

        val response = handler.handleIllegalArgumentException(
            IllegalArgumentException("Invalid user <img src=x onerror=alert(1)>"),
            request
        )

        val body = response.body
        assertNotNull(body)
        assertEquals(400, body!!.status)
        assertFalse(body.message.contains("<img"))
        assertTrue(body.message.contains("&lt;img src=x onerror=alert(1)&gt;"))
        assertFalse(body.path.contains("<script>"))
        assertTrue(body.path.contains("&lt;script&gt;alert(1)&lt;/script&gt;"))
    }

    @Test
    fun `handleEntityNotFoundException escapes reflected message`() {
        val request = MockHttpServletRequest("GET", "/api/v1/users/123")

        val response = handler.handleEntityNotFoundException(
            EntityNotFoundException("User <svg/onload=alert(1)> not found"),
            request
        )

        val body = response.body
        assertNotNull(body)
        assertEquals(404, body!!.status)
        assertFalse(body.message.contains("<svg"))
        assertTrue(body.message.contains("&lt;svg/onload=alert(1)&gt;"))
    }

    @Test
    fun `handleMissingRequestParameter escapes reflected parameter name`() {
        val request = MockHttpServletRequest("GET", "/api/v1/users")

        val response = handler.handleMissingRequestParameter(
            MissingServletRequestParameterException("sort<script>", "String"),
            request
        )

        val body = response.body
        assertNotNull(body)
        assertEquals(400, body!!.status)
        assertFalse(body.message.contains("<script>"))
        assertTrue(body.message.contains("sort&lt;script&gt;"))
    }
}

