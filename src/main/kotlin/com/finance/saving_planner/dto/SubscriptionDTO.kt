package com.finance.saving_planner.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import java.util.UUID

data class SubscriptionDTO(
    val id: UUID?,
    @NotBlank
    val subscriptionName: String,
    @PositiveOrZero
    val subscriptionCost: Double
)