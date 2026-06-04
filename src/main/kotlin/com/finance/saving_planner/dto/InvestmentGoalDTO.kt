package com.finance.saving_planner.dto

import java.util.Date
import java.util.UUID

data class InvestmentGoalDTO(
    val id: UUID? = null,
    val purpose: String,
    val targetAmount: Double,
    val startDate: Date?,
    val targetDate: Date?,
    val startingAmount: Double = 0.0,
    val expectedAnnualReturnRate: Double = 0.0,
    // Computed on read, ignored on write
    val currentAmount: Double? = null,
    val averageMonthlyContribution: Double? = null,
)
