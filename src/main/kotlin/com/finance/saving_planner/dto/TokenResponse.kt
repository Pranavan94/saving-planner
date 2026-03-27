package com.finance.saving_planner.dto

import java.util.UUID

data class TokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val userId: UUID,
    val email: String,
    val role: String,
)

