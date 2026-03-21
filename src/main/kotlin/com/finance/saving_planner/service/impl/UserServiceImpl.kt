package com.finance.saving_planner.service.impl

import com.fasterxml.jackson.databind.JsonNode
import com.finance.saving_planner.dto.AllUserInfoDTO
import com.finance.saving_planner.dto.CreateUserRequest
import com.finance.saving_planner.model.User
import com.finance.saving_planner.repository.UserRepository
import com.finance.saving_planner.service.UserService
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : UserService {

    override fun createUser(request: CreateUserRequest): String {
        val user = User(
            companyId = request.companyId,
            email = request.email,
            firstName = request.firstName,
            middleName = request.middleName,
            lastName = request.lastName,
            passwordHash = passwordEncoder.encode(request.password),
            role = request.role,
            telephoneNumber = normalizeTelephoneNumber(request.telephoneNumber),
            onboardingDone = request.onboardingDone,
        )

        userRepository.save(user)
        return "User ${user.firstName} ${user.lastName} created successfully"
    }

    private fun normalizeTelephoneNumber(telephoneNumber: String?): Long? {
        val rawValue = telephoneNumber?.trim().orEmpty()
        if (rawValue.isBlank()) {
            return null
        }

        val digitsOnly = rawValue.filter(Char::isDigit)
        if (digitsOnly.isBlank()) {
            throw IllegalArgumentException("Invalid telephone number format")
        }

        return digitsOnly.toLongOrNull()
            ?: throw IllegalArgumentException("Telephone number is too large")
    }

    override fun getUser(userId: UUID): User {
        // Fetch the user by ID from the database
        return userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User with ID $userId not found")
        }
    }

    override fun getAllUsers(): Collection<User> {
        // Fetch all users from the database
        return userRepository.findAll()
    }

    override fun updateUser(userId: UUID, user: JsonNode): String {
        // Update the user in the database
        val existingUser = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User with ID $userId not found") }

        val updatedUser = existingUser.copy(
            email = user["email"]?.asText() ?: existingUser.email,
            firstName = user["firstName"]?.asText() ?: existingUser.firstName,
            middleName = user["middleName"]?.asText() ?: existingUser.middleName,
            lastName = user["lastName"]?.asText() ?: existingUser.lastName,
            telephoneNumber = extractTelephoneNumber(user) ?: existingUser.telephoneNumber,
            onboardingDone = true,
            updatedAt = LocalDateTime.now(), // Always update the timestamp
        )
        userRepository.save(updatedUser)
        return "User ${updatedUser.firstName} ${updatedUser.lastName}  updated successfully"
    }

    private fun extractTelephoneNumber(user: JsonNode): Long? {
        val phoneNode = user["telephoneNumber"] ?: user["phoneNumber"] ?: return null

        return when {
            phoneNode.isNumber -> phoneNode.asLong()
            phoneNode.isTextual -> normalizeTelephoneNumber(phoneNode.asText())
            phoneNode.isNull -> null
            else -> throw IllegalArgumentException("Invalid telephone number format")
        }
    }

    override fun getAllUserInfoById(userId: UUID): AllUserInfoDTO {
        return userRepository.getAllUserInfoDTO(userId)
            ?: throw EntityNotFoundException("User with ID $userId not found")
    }

    override fun deleteUser(userId: UUID): String {
        // Delete the user by ID
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId)
            return "User with ID $userId deleted successfully"
        } else {
            throw EntityNotFoundException("User with ID $userId not found")
        }
    }
}