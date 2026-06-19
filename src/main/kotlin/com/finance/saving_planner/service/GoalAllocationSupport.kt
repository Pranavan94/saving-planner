package com.finance.saving_planner.service

import com.finance.saving_planner.dto.GoalMonthlyAllocationOverrideDTO
import com.finance.saving_planner.model.GoalMonthlyAllocationOverride
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date

object GoalAllocationSupport {
    private val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    fun validatePlan(
        defaultAllocationPercentage: Double?,
        monthlyAllocationOverrides: Collection<GoalMonthlyAllocationOverrideDTO>,
        startDate: Date?,
        targetDate: Date?,
    ) {
        defaultAllocationPercentage?.let(::validatePercentage)

        val seenMonths = mutableSetOf<String>()
        monthlyAllocationOverrides.forEach { override ->
            val normalizedMonth = normalizeMonth(override.month)
            require(seenMonths.add(normalizedMonth)) {
                "Duplicate monthly allocation override for month $normalizedMonth"
            }
            override.overridePercentage?.let(::validatePercentage)
            override.allocatedAmount?.let {
                require(it >= 0) { "allocatedAmount cannot be negative for month $normalizedMonth" }
            }
            require(override.overridePercentage != null || override.allocatedAmount != null) {
                "Monthly allocation override for month $normalizedMonth must include overridePercentage or allocatedAmount"
            }
            validateMonthWithinRange(normalizedMonth, startDate, targetDate)
        }
    }

    fun toModel(overrides: Collection<GoalMonthlyAllocationOverrideDTO>): MutableList<GoalMonthlyAllocationOverride> =
        overrides
            .map {
                GoalMonthlyAllocationOverride(
                    month = normalizeMonth(it.month),
                    overridePercentage = it.overridePercentage,
                    allocatedAmount = it.allocatedAmount,
                    updatedAt = it.updatedAt ?: LocalDateTime.now(),
                )
            }
            .sortedBy { it.month }
            .toMutableList()

    fun toDto(overrides: Collection<GoalMonthlyAllocationOverride>): List<GoalMonthlyAllocationOverrideDTO> =
        overrides
            .sortedBy { it.month }
            .map {
                GoalMonthlyAllocationOverrideDTO(
                    month = it.month,
                    overridePercentage = it.overridePercentage,
                    allocatedAmount = it.allocatedAmount,
                    updatedAt = it.updatedAt,
                )
            }

    fun normalizeMonth(month: String): String = parseMonth(month).format(monthFormatter)

    fun parseMonth(month: String): YearMonth =
        try {
            YearMonth.parse(month.trim(), monthFormatter)
        } catch (_: DateTimeParseException) {
            throw IllegalArgumentException("Month '$month' must use yyyy-MM format")
        }

    fun toMonth(date: Date): YearMonth = YearMonth.from(date.toInstant().atZone(ZoneOffset.UTC))

    private fun validatePercentage(percentage: Double) {
        require(percentage in 0.0..100.0) { "Allocation percentage must be between 0 and 100" }
    }

    private fun validateMonthWithinRange(month: String, startDate: Date?, targetDate: Date?) {
        val yearMonth = parseMonth(month)
        startDate?.let {
            require(!yearMonth.isBefore(toMonth(it))) {
                "Month $month cannot be before the goal start month ${toMonth(it).format(monthFormatter)}"
            }
        }
        targetDate?.let {
            require(!yearMonth.isAfter(toMonth(it))) {
                "Month $month cannot be after the goal target month ${toMonth(it).format(monthFormatter)}"
            }
        }
    }
}

