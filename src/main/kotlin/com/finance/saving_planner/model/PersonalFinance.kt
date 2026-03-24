package com.finance.saving_planner.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
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
    val startDate: Date?,
    val endDate: Date?,
    val monthlyIncome: Double,
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val monthlyExpenses: MonthlyExpenses,
    val consumption: Double,
    val savings: Double?,
    val investments: Double?,
    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)