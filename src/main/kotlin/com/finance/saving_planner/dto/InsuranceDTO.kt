package com.finance.saving_planner.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import java.util.UUID

data class InsuranceDTO(
    val id: UUID?,
    @NotBlank
    val insuranceType: String,
    @PositiveOrZero
    val insuranceCost: Double,
    @NotBlank
    val insuranceCompany: String
)