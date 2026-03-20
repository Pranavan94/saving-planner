package com.finance.saving_planner.dto

import java.util.Date
import java.util.UUID

data class PersonalFinanceOverviewDTO(
    val id: UUID,
    val startDate: Date?,
    val endDate: Date?,
    val monthlyIncome: Double,
    val monthlyExpenses: Double,
    val consumption: Double,
    val savings: Double?,
    val investments: Double?,
    val mortgagePayment: Double?,
    val foodBudget: Double?,
)