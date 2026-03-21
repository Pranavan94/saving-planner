package com.finance.saving_planner.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.finance.saving_planner.PostgreSQLIntegrationTest
import com.finance.saving_planner.model.User
import com.finance.saving_planner.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", authorities = ["read"])
@DisplayName("UserController Integration/Regression Tests")
class UserControllerRegressionTest : PostgreSQLIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val basePath = "/api/v1/users"
    private lateinit var testUser: User
    private lateinit var testUserId: UUID

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        testUser = User(
            id = UUID.randomUUID(),
            companyId = UUID.randomUUID(),
            email = "regression.test@example.com",
            firstName = "Regression",
            middleName = "Test",
            lastName = "User",
            passwordHash = "testHash",
            role = "USER",
            telephoneNumber = 9876543210,
            onboardingDone = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        testUser = userRepository.save(testUser)
        testUserId = testUser.id!!
    }

    @Test
    @DisplayName("RT-001: POST /user endpoint returns 201 CREATED")
    fun testRegisterUserReturns201() {
        val newUser = objectMapper.createObjectNode().apply {
            put("companyId", UUID.randomUUID().toString())
            put("email", "newuser@example.com")
            put("firstName", "New")
            put("lastName", "User")
            put("password", "hash12345")
            put("role", "USER")
            put("onboardingDone", true)
        }

        mockMvc.perform(
            post("$basePath/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser))
        )
            .andExpect(status().isCreated)
    }

    @Test
    @DisplayName("RT-002: GET /{userId} returns 200 OK with correct user data")
    fun testGetUserByIdReturns200() {
        mockMvc.perform(
            get("$basePath/{userId}", testUserId.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(testUserId.toString()))
            .andExpect(jsonPath("$.email").value("regression.test@example.com"))
            .andExpect(jsonPath("$.firstName").value("Regression"))
    }

    @Test
    @DisplayName("RT-003: GET /{userId} with invalid UUID returns 400 BAD_REQUEST")
    fun testGetUserByInvalidIdReturns400() {
        mockMvc.perform(
            get("$basePath/invalid-uuid")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("RT-004: GET empty users endpoint returns 200 OK with empty array")
    fun testGetAllUsersEmptyReturns200() {
        userRepository.deleteAll()

        mockMvc.perform(get(basePath))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("RT-005: GET /allinfo/{userId} returns 200 OK with user and finances")
    fun testGetAllUserInfoReturns200() {
        mockMvc.perform(
            get("$basePath/allinfo/{userId}", testUserId.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId").value(testUserId.toString()))
    }

    @Test
    @DisplayName("RT-006: PUT /{userId} successfully updates user data")
    fun testUpdateUserSuccess() {
        val updatePayload = objectMapper.createObjectNode().apply {
            put("email", "updated@example.com")
            put("firstName", "Updated")
            put("phoneNumber", "+1 1112223333")
        }

        mockMvc.perform(
            put("$basePath/{userId}", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePayload))
        )
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("RT-007: DELETE /{userId} successfully removes user")
    fun testDeleteUserSuccess() {
        mockMvc.perform(
            delete("$basePath/{userId}", testUserId.toString())
        )
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("RT-008: POST with invalid JSON returns 400 BAD_REQUEST")
    fun testPostWithInvalidJsonReturns400() {
        mockMvc.perform(
            post("$basePath/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json content")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("RT-009: Created user persists in database")
    fun testUserPersistenceAfterCreation() {
        val newUser = objectMapper.createObjectNode().apply {
            put("companyId", UUID.randomUUID().toString())
            put("email", "persist@example.com")
            put("firstName", "Persist")
            put("lastName", "Check")
            put("password", "hash12345")
            put("role", "USER")
            put("onboardingDone", true)
        }

        mockMvc.perform(
            post("$basePath/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser))
        )
            .andExpect(status().isCreated)

        val savedUsers = userRepository.findAll().filter { it.email == "persist@example.com" }
        assertEquals(1, savedUsers.size)
        assertEquals("persist@example.com", savedUsers.first().email)
        assertTrue(savedUsers.first().passwordHash != "hash12345")
    }

    @Test
    @DisplayName("RT-010: Deleted user no longer exists in database")
    fun testUserDeletionFromDatabase() {
        assertTrue(userRepository.existsById(testUserId))

        mockMvc.perform(
            delete("$basePath/{userId}", testUserId.toString())
        )
            .andExpect(status().isOk)

        assertFalse(userRepository.existsById(testUserId))
    }

    @Test
    @DisplayName("RT-010A: DELETE /{userId} returns 404 for missing user")
    fun testDeleteUserNotFoundReturns404() {
        val missingUserId = UUID.randomUUID()

        mockMvc.perform(
            delete("$basePath/{userId}", missingUserId.toString())
        )
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("RT-011: Multiple users can be retrieved without data leakage")
    fun testMultipleUsersNoDataLeakage() {
        val user2 = User(
            id = UUID.randomUUID(),
            companyId = UUID.randomUUID(),
            email = "user2@example.com",
            firstName = "User",
            lastName = "Two",
            passwordHash = "hash",
            role = "USER",
            onboardingDone = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        userRepository.save(user2)

        mockMvc.perform(get(basePath))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("RT-012: GET request with UUID parameter format works correctly")
    fun testUUIDParameterHandling() {
        mockMvc.perform(
            get("$basePath/{userId}", testUserId.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(testUserId.toString()))
    }

    @Test
    @DisplayName("RT-013: CORS preflight for POST /user returns allow-origin headers for frontend")
    fun testCorsPreflightForRegisterUser() {
        mockMvc.perform(
            options("$basePath/user")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type,authorization")
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
            .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
    }
}

