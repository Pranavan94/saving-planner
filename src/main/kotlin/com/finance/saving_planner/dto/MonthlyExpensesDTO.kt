package com.finance.saving_planner.dto

import jakarta.validation.constraints.PositiveOrZero
import java.util.UUID

data class MonthlyExpensesDTO(
    val id: UUID?,
    @PositiveOrZero
    val mortgagePayment: Double,
    @PositiveOrZero
    val sharedHouseCost: Double,
    @PositiveOrZero
    val foodBudget: Double,
    val carLoan: Double?,
    val creditCardBill: Double?,
    val electricityBill: Double?,
    val studentLoans: Double?,
    val tollFees: Double?,
    val insurances: List<InsuranceDTO>?,
    val subscriptions: List<SubscriptionDTO>?
)
