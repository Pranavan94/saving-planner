package com.finance.saving_planner.service.impl

import com.finance.saving_planner.dto.GoalMonthlyAllocationOverrideDTO
import com.finance.saving_planner.dto.SavingsGoalDTO
import com.finance.saving_planner.model.SavingsGoal
import com.finance.saving_planner.repository.SavingsGoalRepository
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

class SavingsGoalServiceImplTest {

    @Mock
    private lateinit var savingsGoalRepository: SavingsGoalRepository

    @Mock
    private lateinit var goalProgressCalculator: GoalProgressCalculator

    @InjectMocks
    private lateinit var savingsGoalService: SavingsGoalServiceImpl

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun createSavingsGoalPersistsDefaultPercentageAndMonthlyOverrides() {
        val goalId = UUID.randomUUID()
        val dto = SavingsGoalDTO(
            purpose = "Vacation",
            targetAmount = 5000.0,
            startDate = date("2026-02-01T00:00:00Z"),
            targetDate = date("2026-07-31T00:00:00Z"),
            startingAmount = 250.0,
            defaultAllocationPercentage = 35.0,
            monthlyAllocationOverrides = listOf(
                GoalMonthlyAllocationOverrideDTO(month = "2026-03", overridePercentage = 50.0),
                GoalMonthlyAllocationOverrideDTO(month = "2026-04", allocatedAmount = 400.0),
            ),
        )
        val savedGoal = SavingsGoal(
            id = goalId,
            purpose = dto.purpose,
            targetAmount = dto.targetAmount,
            startDate = dto.startDate,
            targetDate = dto.targetDate,
            startingAmount = dto.startingAmount,
            defaultAllocationPercentage = dto.defaultAllocationPercentage,
            monthlyAllocationOverrides = com.finance.saving_planner.service.GoalAllocationSupport.toModel(dto.monthlyAllocationOverrides),
        )
        val goalCaptor = argumentCaptor<SavingsGoal>()

        whenever(savingsGoalRepository.save(any())).thenReturn(savedGoal)
        whenever(goalProgressCalculator.calculate(anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .thenReturn(GoalProgressCalculator.Progress(contributed = 600.0, averageMonthlyContribution = 300.0))

        val result = savingsGoalService.createSavingsGoal(dto)

        verify(savingsGoalRepository).save(goalCaptor.capture())
        assertAll(
            { assertEquals(35.0, goalCaptor.firstValue.defaultAllocationPercentage) },
            { assertEquals(2, goalCaptor.firstValue.monthlyAllocationOverrides.size) },
            { assertEquals("2026-03", goalCaptor.firstValue.monthlyAllocationOverrides[0].month) },
            { assertEquals(50.0, goalCaptor.firstValue.monthlyAllocationOverrides[0].overridePercentage) },
            { assertEquals(400.0, goalCaptor.firstValue.monthlyAllocationOverrides[1].allocatedAmount) },
            { assertEquals(goalId, result.id) },
            { assertEquals(35.0, result.defaultAllocationPercentage) },
            { assertEquals(2, result.monthlyAllocationOverrides.size) },
            { assertEquals(850.0, result.currentAmount) },
            { assertEquals(300.0, result.averageMonthlyContribution) },
        )
    }

    @Test
    fun createSavingsGoalRejectsDuplicateOverrideMonths() {
        val dto = SavingsGoalDTO(
            purpose = "Car",
            targetAmount = 10000.0,
            startDate = date("2026-01-01T00:00:00Z"),
            targetDate = date("2026-12-31T00:00:00Z"),
            defaultAllocationPercentage = 40.0,
            monthlyAllocationOverrides = listOf(
                GoalMonthlyAllocationOverrideDTO(month = "2026-05", overridePercentage = 60.0),
                GoalMonthlyAllocationOverrideDTO(month = "2026-05", allocatedAmount = 300.0),
            ),
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            savingsGoalService.createSavingsGoal(dto)
        }

        assertEquals("Duplicate monthly allocation override for month 2026-05", exception.message)
    }

    private fun date(instant: String): Date = Date.from(Instant.parse(instant))
}


