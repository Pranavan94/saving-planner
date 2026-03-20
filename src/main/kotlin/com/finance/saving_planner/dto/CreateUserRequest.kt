package com.finance.saving_planner.dto

import com.fasterxml.jackson.annotation.JsonAlias
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateUserRequest(
    val companyId: UUID? = null,
    @field:Email
    @field:NotBlank
    val email: String,
    @field:NotBlank
    @field:Size(min = 1, max = 50)
    val firstName: String,
    val middleName: String? = null,
    @field:NotBlank
    @field:Size(min = 1, max = 50)
    val lastName: String,
    @field:NotBlank
    @field:Size(min = 8, max = 255)
    @field:JsonAlias("passwordHash")
    val password: String,
    @field:NotBlank
    val role: String,
    @field:JsonAlias("phoneNumber")
    val telephoneNumber: String? = null,
    val onboardingDone: Boolean = false,
)

