package com.finance.saving_planner.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.LocalDateTime

@Embeddable
data class GoalMonthlyAllocationOverride(
    @Column(name = "plan_month", nullable = false, length = 7)
    val month: String,
    @Column(name = "override_percentage")
    val overridePercentage: Double? = null,
    @Column(name = "allocated_amount")
    val allocatedAmount: Double? = null,
    @Column(name = "override_updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

