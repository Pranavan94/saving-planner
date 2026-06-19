package com.finance.saving_planner.service

import com.finance.saving_planner.model.GoalMonthlyAllocationOverride
import com.finance.saving_planner.model.MonthlyExpenses
import com.finance.saving_planner.model.PersonalFinance
import com.finance.saving_planner.repository.PersonalFinanceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Date

class GoalProgressCalculatorTest {

    @Mock
    private lateinit var personalFinanceRepository: PersonalFinanceRepository

    private lateinit var goalProgressCalculator: GoalProgressCalculator

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        goalProgressCalculator = GoalProgressCalculator(personalFinanceRepository)
    }

    @Test
    fun calculateUsesDefaultPercentagesOverridesAndAllocatedSnapshotsPerMonth() {
        whenever(personalFinanceRepository.findAll()).thenReturn(
            listOf(
                finance("2026-03-01T00:00:00Z", savings = 1000.0, investments = 300.0),
                finance("2026-04-01T00:00:00Z", savings = 800.0, investments = 250.0),
                finance("2026-05-01T00:00:00Z", savings = 700.0, investments = 200.0),
                finance("2026-06-01T00:00:00Z", savings = 900.0, investments = 400.0),
            ),
        )

        val progress = goalProgressCalculator.calculate(
            startDate = date("2026-03-01T00:00:00Z"),
            targetDate = date("2026-05-31T00:00:00Z"),
            defaultAllocationPercentage = 50.0,
            monthlyAllocationOverrides = listOf(
                GoalMonthlyAllocationOverride(month = "2026-03", overridePercentage = 60.0),
                GoalMonthlyAllocationOverride(month = "2026-04", allocatedAmount = 350.0),
            ),
        ) { it.savings }

        assertEquals(1300.0, progress.contributed, 0.0001)
        assertEquals(1300.0 / 3.0, progress.averageMonthlyContribution, 0.0001)
    }

    @Test
    fun calculateFallsBackToLegacyFullAmountWhenNoPlanningDataExists() {
        whenever(personalFinanceRepository.findAll()).thenReturn(
            listOf(
                finance("2026-03-01T00:00:00Z", savings = 900.0, investments = 100.0),
                finance("2026-04-01T00:00:00Z", savings = 600.0, investments = 250.0),
            ),
        )

        val progress = goalProgressCalculator.calculate(
            startDate = date("2026-03-01T00:00:00Z"),
            targetDate = date("2026-04-30T00:00:00Z"),
            defaultAllocationPercentage = null,
            monthlyAllocationOverrides = emptyList(),
        ) { it.savings }

        assertEquals(1500.0, progress.contributed, 0.0001)
        assertEquals(750.0, progress.averageMonthlyContribution, 0.0001)
    }

    private fun finance(instant: String, savings: Double, investments: Double): PersonalFinance =
        PersonalFinance(
            startDate = date(instant),
            endDate = date(instant),
            monthlyIncome = 5000.0,
            monthlyExpenses = monthlyExpenses(),
            savings = savings,
            investments = investments,
        )

    private fun monthlyExpenses(): MonthlyExpenses =
        MonthlyExpenses(
            mortgagePayment = 1000.0,
            sharedHouseCost = 200.0,
            foodBudget = 300.0,
            carLoan = null,
            creditCardBill = null,
            electricityBill = null,
            studentLoans = null,
            tollFees = null,
            insurances = mutableListOf(),
            subscriptions = mutableListOf(),
        )

    private fun date(instant: String): Date = Date.from(Instant.parse(instant))
}

