package com.finance.saving_planner.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.finance.saving_planner.dto.CreateUserRequest
import com.finance.saving_planner.dto.MessageResponse
import com.finance.saving_planner.model.User
import com.finance.saving_planner.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("UserController Unit Tests")
class UserControllerTest {

    @Mock
    private lateinit var userService: UserService

    @InjectMocks
    private lateinit var userController: UserController

    private val objectMapper = ObjectMapper()
    private val testUserId = UUID.randomUUID()
    private val createUserRequest = CreateUserRequest(
        companyId = UUID.randomUUID(),
        email = "test@example.com",
        firstName = "John",
        middleName = "Michael",
        lastName = "Doe",
        password = "plainPassword123",
        role = "USER",
        telephoneNumber = "+1 1234567890",
        onboardingDone = true,
    )
    private val testUser = User(
        id = testUserId,
        companyId = createUserRequest.companyId,
        email = "test@example.com",
        firstName = "John",
        middleName = "Michael",
        lastName = "Doe",
        passwordHash = "hashedPassword123",
        role = "USER",
        telephoneNumber = 1234567890,
        onboardingDone = true,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    @DisplayName("Should register a new user successfully")
    fun testRegisterUserSuccess() {
        // Arrange
        val expectedMessage = "User John Doe created successfully"
        whenever(userService.createUser(any())).thenReturn(expectedMessage)

        // Act
        val result = userController.registerUser(createUserRequest)

        // Assert
        assertEquals(MessageResponse(expectedMessage), result)
        verify(userService).createUser(any())
    }

    @Test
    @DisplayName("Should return user by valid UUID")
    fun testFindUserByIdSuccess() {
        // Arrange
        whenever(userService.getUser(testUserId)).thenReturn(testUser)

        // Act
        val result = userController.findUserById(testUserId)

        // Assert
        assertNotNull(result)
        assertEquals(testUser.id, result.id)
        assertEquals(testUser.email, result.email)
        assertEquals(testUser.firstName, result.firstName)
        verify(userService).getUser(testUserId)
    }

    @Test
    @DisplayName("Should throw exception when user not found by UUID")
    fun testFindUserByIdNotFound() {
        // Arrange
        val nonExistentId = UUID.randomUUID()
        whenever(userService.getUser(nonExistentId))
            .thenThrow(IllegalArgumentException("User with ID $nonExistentId not found"))

        // Act & Assert
        assertThrows<IllegalArgumentException> {
            userController.findUserById(nonExistentId)
        }
        verify(userService).getUser(nonExistentId)
    }

    @Test
    @DisplayName("Should retrieve all users successfully")
    fun testGetAllUsersSuccess() {
        // Arrange
        val user2Id = UUID.randomUUID()
        val user2 = User(
            id = user2Id,
            companyId = UUID.randomUUID(),
            email = "user2@example.com",
            firstName = "Jane",
            middleName = "Mary",
            lastName = "Smith",
            passwordHash = "hash2",
            role = "USER",
            telephoneNumber = 5555555555,
            onboardingDone = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val users = listOf(testUser, user2)
        whenever(userService.getAllUsers()).thenReturn(users)

        // Act
        val result = userController.getAllUsers()

        // Assert
        assertNotNull(result)
        assertEquals(2, result.size)
        verify(userService).getAllUsers()
    }

    @Test
    @DisplayName("Should return empty collection when no users exist")
    fun testGetAllUsersEmpty() {
        // Arrange
        whenever(userService.getAllUsers()).thenReturn(emptyList())

        // Act
        val result = userController.getAllUsers()

        // Assert
        assertNotNull(result)
        assertEquals(0, result.size)
        verify(userService).getAllUsers()
    }

    @Test
    @DisplayName("Should update user successfully")
    fun testUpdateUserSuccess() {
        // Arrange
        val updateJson: JsonNode = objectMapper.createObjectNode().apply {
            put("id", testUserId.toString())
            put("email", "newemail@example.com")
            put("firstName", "Jane")
        }
        val expectedMessage = "User updated successfully"
        whenever(userService.updateUser(any())).thenReturn(expectedMessage)

        // Act
        val result = userController.updateUser(updateJson)

        // Assert
        assertEquals(expectedMessage, result)
        verify(userService).updateUser(any())
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent user")
    fun testUpdateUserNotFound() {
        // Arrange
        val nonExistentId = UUID.randomUUID()
        val updateJson: JsonNode = objectMapper.createObjectNode().apply {
            put("id", nonExistentId.toString())
            put("email", "newemail@example.com")
        }
        whenever(userService.updateUser(any()))
            .thenThrow(IllegalArgumentException("User not found"))

        // Act & Assert
        assertThrows<IllegalArgumentException> {
            userController.updateUser(updateJson)
        }
    }

    @Test
    @DisplayName("Should delete user successfully")
    fun testRemoveUserSuccess() {
        // Arrange
        val expectedMessage = "User deleted successfully"
        whenever(userService.deleteUser(testUserId)).thenReturn(expectedMessage)

        // Act
        val result = userController.removeUser(testUserId)

        // Assert
        assertEquals(expectedMessage, result)
        verify(userService).deleteUser(testUserId)
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    fun testRemoveUserNotFound() {
        // Arrange
        val nonExistentId = UUID.randomUUID()
        whenever(userService.deleteUser(nonExistentId))
            .thenThrow(IllegalArgumentException("User not found"))

        // Act & Assert
        assertThrows<IllegalArgumentException> {
            userController.removeUser(nonExistentId)
        }
    }
}

