package com.finance.saving_planner.dto

import java.time.LocalDateTime

/**
 * A persisted month-specific allocation override for a goal.
 *
 * month uses yyyy-MM format.
 * overridePercentage overrides the goal default percentage for that month.
 * allocatedAmount can optionally capture the actual amount allocated for audit/history.
 */
data class GoalMonthlyAllocationOverrideDTO(
    val month: String,
    val overridePercentage: Double? = null,
    val allocatedAmount: Double? = null,
    val updatedAt: LocalDateTime? = null,
)

