package com.finance.saving_planner.service

import com.finance.saving_planner.model.GoalMonthlyAllocationOverride
import com.finance.saving_planner.model.PersonalFinance
import com.finance.saving_planner.repository.PersonalFinanceRepository
import org.springframework.stereotype.Component
import java.time.YearMonth
import java.util.Date

/**
 * Aggregates how much has actually been contributed toward a goal by summing the
 * relevant per-period amounts (savings or investments) from existing PersonalFinance
 * records that fall within the goal's window (from startDate up to now / targetDate).
 */
@Component
class GoalProgressCalculator(private val personalFinanceRepository: PersonalFinanceRepository) {

    data class Progress(val contributed: Double, val averageMonthlyContribution: Double)

    fun calculate(
        startDate: Date?,
        targetDate: Date?,
        defaultAllocationPercentage: Double?,
        monthlyAllocationOverrides: Collection<GoalMonthlyAllocationOverride>,
        amountSelector: (PersonalFinance) -> Double?,
    ): Progress {
        val now = Date()
        val windowEnd = listOfNotNull(targetDate, now).min()
        val hasPlanningData = defaultAllocationPercentage != null || monthlyAllocationOverrides.isNotEmpty()

        val monthlyAmounts = personalFinanceRepository.findAll()
            .filter { finance ->
                val periodDate = finance.startDate ?: finance.endDate ?: return@filter false
                val afterStart = startDate == null || !periodDate.before(startDate)
                val beforeEnd = !periodDate.after(windowEnd)
                afterStart && beforeEnd
            }
            .groupBy { finance ->
                val periodDate = finance.startDate ?: finance.endDate!!
                GoalAllocationSupport.toMonth(periodDate)
            }
            .mapValues { (_, finances) -> finances.sumOf { amountSelector(it) ?: 0.0 } }

        val relevantOverrideMap = monthlyAllocationOverrides
            .filter { override ->
                val overrideMonth = GoalAllocationSupport.parseMonth(override.month)
                isMonthWithinWindow(overrideMonth, startDate, windowEnd)
            }
            .associateBy { GoalAllocationSupport.parseMonth(it.month) }

        val relevantMonths = (monthlyAmounts.keys + relevantOverrideMap.keys).toSortedSet()

        if (relevantMonths.isEmpty()) {
            return Progress(contributed = 0.0, averageMonthlyContribution = 0.0)
        }

        val monthlyContributions = relevantMonths.map { month ->
            val override = relevantOverrideMap[month]
            val monthAmount = monthlyAmounts[month] ?: 0.0
            val allocatedAmount = override?.allocatedAmount
            val overridePercentage = override?.overridePercentage
            when {
                allocatedAmount != null -> allocatedAmount
                overridePercentage != null -> monthAmount * (overridePercentage / 100.0)
                defaultAllocationPercentage != null -> monthAmount * (defaultAllocationPercentage / 100.0)
                hasPlanningData -> 0.0
                else -> monthAmount
            }
        }

        val contributed = monthlyContributions.sum()
        val average = contributed / relevantMonths.size
        return Progress(contributed = contributed, averageMonthlyContribution = average)
    }

    private fun isMonthWithinWindow(month: YearMonth, startDate: Date?, windowEnd: Date): Boolean {
        val lowerBoundSatisfied = startDate == null || !month.isBefore(GoalAllocationSupport.toMonth(startDate))
        val upperBoundSatisfied = !month.isAfter(GoalAllocationSupport.toMonth(windowEnd))
        return lowerBoundSatisfied && upperBoundSatisfied
    }
}
