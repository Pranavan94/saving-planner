package com.finance.saving_planner.model

import jakarta.persistence.Column
import jakarta.persistence.CollectionTable
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OrderBy
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
    val defaultAllocationPercentage: Double? = null,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "savings_goal_monthly_allocation_override",
        joinColumns = [JoinColumn(name = "goal_id")],
    )
    @OrderBy("month ASC")
    val monthlyAllocationOverrides: MutableList<GoalMonthlyAllocationOverride> = mutableListOf(),
    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
