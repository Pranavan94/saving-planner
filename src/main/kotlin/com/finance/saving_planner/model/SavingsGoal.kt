package com.finance.saving_planner.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

@Entity
@Table(name = "savings_goal")
data class SavingsGoal(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    val id: UUID? = null,
    val purpose: String,
    val targetAmount: Double,
    val startDate: Date?,
    val targetDate: Date?,
    val startingAmount: Double = 0.0,
    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
