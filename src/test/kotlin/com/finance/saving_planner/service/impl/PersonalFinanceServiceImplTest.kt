package com.finance.saving_planner.service.impl

import com.finance.saving_planner.dto.PersonalFinanceOverviewDTO
import com.finance.saving_planner.model.PersonalFinance
import com.finance.saving_planner.repository.PersonalFinanceRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

class PersonalFinanceServiceImplTest {

    @Mock
    private lateinit var personalFinanceRepository: PersonalFinanceRepository

    @InjectMocks
    private lateinit var personalFinanceService: PersonalFinanceServiceImpl

    val personalFinanceDto = PersonalFinanceOverviewDTO(
        id = UUID.randomUUID(),
        startDate = Date.from(Instant.parse("2026-03-01T00:00:00Z")),
        endDate = Date.from(Instant.parse("2026-03-31T00:00:00Z")),
        monthlyIncome = 5000.0,
        monthlyExpenses = 2500.0,
        consumption = 1200.0,
        savings = 900.0,
        investments = 300.0,
        mortgagePayment = 1100.0,
        foodBudget = 450.0,
    )

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun createPersonalFinanceOverviewTest() {
        val savedPersonalFinance = PersonalFinance(
            id = UUID.randomUUID(),
            startDate = personalFinanceDto.startDate,
            endDate = personalFinanceDto.endDate,
            monthlyIncome = 5000.0,
            monthlyExpenses = 2500.0,
            consumption = 1200.0,
            savings = 900.0,
            investments = 300.0,
            mortgagePayment = 1100.0,
            foodBudget = 450.0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
        val financeCaptor = argumentCaptor<PersonalFinance>()

        whenever(personalFinanceRepository.save(any())).thenReturn(savedPersonalFinance)

        val result = personalFinanceService.createPersonalFinanceOverview(personalFinanceDto)

        verify(personalFinanceRepository).save(financeCaptor.capture())
        assertEquals(personalFinanceDto.startDate, financeCaptor.firstValue.startDate)
        assertEquals(personalFinanceDto.endDate, financeCaptor.firstValue.endDate)
        assertEquals(5000.0, financeCaptor.firstValue.monthlyIncome)
        assertEquals(2500.0, financeCaptor.firstValue.monthlyExpenses)
        assertEquals(1200.0, financeCaptor.firstValue.consumption)
        assertEquals(900.0, financeCaptor.firstValue.savings)
        assertEquals(300.0, financeCaptor.firstValue.investments)
        assertEquals(1100.0, financeCaptor.firstValue.mortgagePayment)
        assertEquals(450.0, financeCaptor.firstValue.foodBudget)
        assertSame(savedPersonalFinance, result)
        assertEquals(savedPersonalFinance.id, result.id)

    }

}