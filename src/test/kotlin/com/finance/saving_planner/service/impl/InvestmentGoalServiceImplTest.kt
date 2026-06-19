package com.finance.saving_planner.service.impl

import com.finance.saving_planner.dto.GoalMonthlyAllocationOverrideDTO
import com.finance.saving_planner.dto.InvestmentGoalDTO
import com.finance.saving_planner.model.InvestmentGoal
import com.finance.saving_planner.repository.InvestmentGoalRepository
import com.finance.saving_planner.service.GoalProgressCalculator
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Date
import java.util.UUID

class InvestmentGoalServiceImplTest {

    @Mock
    private lateinit var investmentGoalRepository: InvestmentGoalRepository

    @Mock
    private lateinit var goalProgressCalculator: GoalProgressCalculator

    @InjectMocks
    private lateinit var investmentGoalService: InvestmentGoalServiceImpl

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun createInvestmentGoalPersistsDefaultPercentageAndMonthlyOverrides() {
        val goalId = UUID.randomUUID()
        val dto = InvestmentGoalDTO(
            purpose = "Retirement",
            targetAmount = 150000.0,
            startDate = date("2026-01-01T00:00:00Z"),
            targetDate = date("2026-12-31T00:00:00Z"),
            startingAmount = 1000.0,
            expectedAnnualReturnRate = 7.0,
            defaultAllocationPercentage = 20.0,
            monthlyAllocationOverrides = listOf(
                GoalMonthlyAllocationOverrideDTO(month = "2026-02", overridePercentage = 30.0),
                GoalMonthlyAllocationOverrideDTO(month = "2026-03", allocatedAmount = 450.0),
            ),
        )
        val savedGoal = InvestmentGoal(
            id = goalId,
            purpose = dto.purpose,
            targetAmount = dto.targetAmount,
            startDate = dto.startDate,
            targetDate = dto.targetDate,
            startingAmount = dto.startingAmount,
            expectedAnnualReturnRate = dto.expectedAnnualReturnRate,
            defaultAllocationPercentage = dto.defaultAllocationPercentage,
            monthlyAllocationOverrides = com.finance.saving_planner.service.GoalAllocationSupport.toModel(dto.monthlyAllocationOverrides),
        )
        val goalCaptor = argumentCaptor<InvestmentGoal>()

        whenever(investmentGoalRepository.save(any())).thenReturn(savedGoal)
        whenever(goalProgressCalculator.calculate(anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .thenReturn(GoalProgressCalculator.Progress(contributed = 900.0, averageMonthlyContribution = 450.0))

        val result = investmentGoalService.createInvestmentGoal(dto)

        verify(investmentGoalRepository).save(goalCaptor.capture())
        assertAll(
            { assertEquals(20.0, goalCaptor.firstValue.defaultAllocationPercentage) },
            { assertEquals(2, goalCaptor.firstValue.monthlyAllocationOverrides.size) },
            { assertEquals("2026-02", goalCaptor.firstValue.monthlyAllocationOverrides[0].month) },
            { assertEquals(30.0, goalCaptor.firstValue.monthlyAllocationOverrides[0].overridePercentage) },
            { assertEquals(450.0, goalCaptor.firstValue.monthlyAllocationOverrides[1].allocatedAmount) },
            { assertEquals(goalId, result.id) },
            { assertEquals(7.0, result.expectedAnnualReturnRate) },
            { assertEquals(20.0, result.defaultAllocationPercentage) },
            { assertEquals(1900.0, result.currentAmount) },
            { assertEquals(450.0, result.averageMonthlyContribution) },
        )
    }

    @Test
    fun createInvestmentGoalRejectsOverrideOutsideGoalWindow() {
        val dto = InvestmentGoalDTO(
            purpose = "Index Fund",
            targetAmount = 8000.0,
            startDate = date("2026-04-01T00:00:00Z"),
            targetDate = date("2026-10-31T00:00:00Z"),
            expectedAnnualReturnRate = 5.0,
            monthlyAllocationOverrides = listOf(
                GoalMonthlyAllocationOverrideDTO(month = "2026-03", overridePercentage = 25.0),
            ),
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            investmentGoalService.createInvestmentGoal(dto)
        }

        assertEquals("Month 2026-03 cannot be before the goal start month 2026-04", exception.message)
    }

    private fun date(instant: String): Date = Date.from(Instant.parse(instant))
}


