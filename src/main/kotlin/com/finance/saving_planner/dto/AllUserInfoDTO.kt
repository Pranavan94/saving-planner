package com.finance.saving_planner.dto

import java.util.UUID

data class AllUserInfoDTO(
    val userId: UUID,
    val email: String,
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val role: String,
    val telephoneNumber: Long?,
    val onboardingDone: Boolean,
)