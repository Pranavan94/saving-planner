package com.finance.saving_planner.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

@Entity
@Table(name = "personal_finance")
data class PersonalFinance (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    val id: UUID? = null,
    val startDate: Date,
    val endDate: Date,
    @NotBlank
    val monthlyIncome: Double,
    @NotBlank
    val monthlyExpenses: Double,
    @NotBlank
    val consumption: Double,
    val savings: Double,
    val investments: Double,
    val mortgagePayment: Double,
    val foodBudget: Double,
    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)