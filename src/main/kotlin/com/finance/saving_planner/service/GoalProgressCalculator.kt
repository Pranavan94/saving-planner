package com.finance.saving_planner.service

import com.finance.saving_planner.model.PersonalFinance
import com.finance.saving_planner.repository.PersonalFinanceRepository
import org.springframework.stereotype.Component
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
        amountSelector: (PersonalFinance) -> Double?,
    ): Progress {
        val now = Date()
        val windowEnd = listOfNotNull(targetDate, now).min()

        val relevantAmounts = personalFinanceRepository.findAll()
            .filter { finance ->
                val periodDate = finance.startDate ?: finance.endDate ?: return@filter false
                val afterStart = startDate == null || !periodDate.before(startDate)
                val beforeEnd = !periodDate.after(windowEnd)
                afterStart && beforeEnd
            }
            .map { amountSelector(it) ?: 0.0 }

        val contributed = relevantAmounts.sum()
        val average = if (relevantAmounts.isEmpty()) 0.0 else contributed / relevantAmounts.size
        return Progress(contributed = contributed, averageMonthlyContribution = average)
    }
}
