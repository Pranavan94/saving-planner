package com.finance.saving_planner.service.impl

import com.finance.saving_planner.dto.InsuranceDTO
import com.finance.saving_planner.dto.MonthlyExpensesDTO
import com.finance.saving_planner.dto.PersonalFinanceOverviewDTO
import com.finance.saving_planner.dto.SubscriptionDTO
import com.finance.saving_planner.model.Insurance
import com.finance.saving_planner.model.MonthlyExpenses
import com.finance.saving_planner.model.PersonalFinance
import com.finance.saving_planner.model.Subscription
import com.finance.saving_planner.repository.PersonalFinanceRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
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

    val insuranceDto = InsuranceDTO(
        id = UUID.randomUUID(),
        insuranceType = "Health Insurance",
        insuranceCost = 200.0,
        insuranceCompany = "CVS Health",
    )

    val subscriptionDto = SubscriptionDTO(
        id = UUID.randomUUID(),
        subscriptionName = "Netflix",
        subscriptionCost = 100.0,
    )

    val monthlyExpenseDto = MonthlyExpensesDTO(
        id = UUID.randomUUID(),
        mortgagePayment = 1100.0,
        sharedHouseCost = 200.0,
        foodBudget = 450.0,
        carLoan = 100.0,
        creditCardBill = 150.0,
        electricityBill = 100.0,
        studentLoans = 50.0,
        tollFees = 20.0,
        insurances = listOf(insuranceDto),
        subscriptions = listOf(subscriptionDto),
    )

    val personalFinanceDto = PersonalFinanceOverviewDTO(
        id = UUID.randomUUID(),
        startDate = Date.from(Instant.parse("2026-03-01T00:00:00Z")),
        endDate = Date.from(Instant.parse("2026-03-31T00:00:00Z")),
        monthlyIncome = 5000.0,
        monthlyExpenses = monthlyExpenseDto,
        consumption = 1200.0,
        savings = 900.0,
        investments = 300.0,
    )

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun createPersonalFinanceOverviewTest() {
        val insurance = Insurance(
            id = UUID.randomUUID(),
            insuranceType = "Health Insurance",
            insuranceCost = 200.0,
            insuranceCompany = "CVS Health",
        )

        val subscription = Subscription(
            id = UUID.randomUUID(),
            subscriptionName = "Netflix",
            subscriptionCost = 100.0,
        )

        val monthlyExpense = MonthlyExpenses(
            id = UUID.randomUUID(),
            mortgagePayment = 1100.0,
            sharedHouseCost = 200.0,
            foodBudget = 450.0,
            carLoan = 100.0,
            creditCardBill = 150.0,
            electricityBill = 100.0,
            studentLoans = 50.0,
            tollFees = 20.0,
            insurances = mutableListOf(insurance),
            subscriptions = mutableListOf(subscription),
        )

        val savedPersonalFinance = PersonalFinance(
            id = UUID.randomUUID(),
            startDate = personalFinanceDto.startDate,
            endDate = personalFinanceDto.endDate,
            monthlyIncome = 5000.0,
            monthlyExpenses = monthlyExpense,
            consumption = 1200.0,
            savings = 900.0,
            investments = 300.0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
        val financeCaptor = argumentCaptor<PersonalFinance>()

        whenever(personalFinanceRepository.save(any())).thenReturn(savedPersonalFinance)

        val result = personalFinanceService.createPersonalFinanceOverview(personalFinanceDto)

        verify(personalFinanceRepository).save(financeCaptor.capture())
        assertAll(
            { assertEquals(personalFinanceDto.startDate, financeCaptor.firstValue.startDate) },
            { assertEquals(personalFinanceDto.endDate, financeCaptor.firstValue.endDate) },
            { assertEquals(5000.0, financeCaptor.firstValue.monthlyIncome) },
            { assertEquals(1200.0, financeCaptor.firstValue.consumption) },
            { assertEquals(900.0, financeCaptor.firstValue.savings) },
            { assertEquals(300.0, financeCaptor.firstValue.investments) },
            { assertEquals(1100.0, financeCaptor.firstValue.monthlyExpenses.mortgagePayment) },
            { assertEquals(1, financeCaptor.firstValue.monthlyExpenses.insurances.size) },
            { assertEquals("Health Insurance", financeCaptor.firstValue.monthlyExpenses.insurances.first().insuranceType) },
            { assertEquals(1, financeCaptor.firstValue.monthlyExpenses.subscriptions.size) },
            { assertEquals("Netflix", financeCaptor.firstValue.monthlyExpenses.subscriptions.first().subscriptionName) },
        )
        assertSame(savedPersonalFinance, result)
        assertEquals(savedPersonalFinance.id, result.id)
    }

    @Test
    fun getPersonalFinanceOverviewMapsEntityToDto() {
        val insurance = Insurance(
            id = UUID.randomUUID(),
            insuranceType = "Health Insurance",
            insuranceCost = 200.0,
            insuranceCompany = "CVS Health",
        )
        val subscription = Subscription(
            id = UUID.randomUUID(),
            subscriptionName = "Netflix",
            subscriptionCost = 100.0,
        )
        val monthlyExpense = MonthlyExpenses(
            id = UUID.randomUUID(),
            mortgagePayment = 1100.0,
            sharedHouseCost = 200.0,
            foodBudget = 450.0,
            carLoan = 100.0,
            creditCardBill = 150.0,
            electricityBill = 100.0,
            studentLoans = 50.0,
            tollFees = 20.0,
            insurances = mutableListOf(insurance),
            subscriptions = mutableListOf(subscription),
        )
        val personalFinance = PersonalFinance(
            id = UUID.randomUUID(),
            startDate = personalFinanceDto.startDate,
            endDate = personalFinanceDto.endDate,
            monthlyIncome = 5000.0,
            monthlyExpenses = monthlyExpense,
            consumption = 1200.0,
            savings = 900.0,
            investments = 300.0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
        val financeId = personalFinance.id!!

        whenever(personalFinanceRepository.findByFinanceId(financeId)).thenReturn(personalFinance)

        val result = personalFinanceService.getPersonalFinanceOverview(financeId)

        assertAll(
            { assertEquals(personalFinance.id, result.id) },
            { assertEquals(monthlyExpense.id, result.monthlyExpenses.id) },
            { assertEquals(1, result.monthlyExpenses.insurances?.size) },
            { assertEquals("Health Insurance", result.monthlyExpenses.insurances?.first()?.insuranceType) },
            { assertEquals(1, result.monthlyExpenses.subscriptions?.size) },
            { assertEquals("Netflix", result.monthlyExpenses.subscriptions?.first()?.subscriptionName) },
            { assertTrue(result.monthlyExpenses.insurances?.first()?.insuranceCost == 200.0) },
        )
    }

    @Test
    fun getTotalOverviewMapsEntitiesToDtos() {
        val firstInsurance = Insurance(
            id = UUID.randomUUID(),
            insuranceType = "Health Insurance",
            insuranceCost = 200.0,
            insuranceCompany = "CVS Health",
        )
        val firstSubscription = Subscription(
            id = UUID.randomUUID(),
            subscriptionName = "Netflix",
            subscriptionCost = 100.0,
        )
        val firstMonthlyExpense = MonthlyExpenses(
            id = UUID.randomUUID(),
            mortgagePayment = 1100.0,
            sharedHouseCost = 200.0,
            foodBudget = 450.0,
            carLoan = 100.0,
            creditCardBill = 150.0,
            electricityBill = 100.0,
            studentLoans = 50.0,
            tollFees = 20.0,
            insurances = mutableListOf(firstInsurance),
            subscriptions = mutableListOf(firstSubscription),
        )
        val firstFinance = PersonalFinance(
            id = UUID.randomUUID(),
            startDate = Date.from(Instant.parse("2026-03-01T00:00:00Z")),
            endDate = Date.from(Instant.parse("2026-03-31T00:00:00Z")),
            monthlyIncome = 5000.0,
            monthlyExpenses = firstMonthlyExpense,
            consumption = 1200.0,
            savings = 900.0,
            investments = 300.0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
        val secondFinance = PersonalFinance(
            id = UUID.randomUUID(),
            startDate = Date.from(Instant.parse("2026-04-01T00:00:00Z")),
            endDate = Date.from(Instant.parse("2026-04-30T00:00:00Z")),
            monthlyIncome = 6500.0,
            monthlyExpenses = MonthlyExpenses(
                id = UUID.randomUUID(),
                mortgagePayment = 1400.0,
                sharedHouseCost = 250.0,
                foodBudget = 500.0,
                carLoan = null,
                creditCardBill = 120.0,
                electricityBill = 90.0,
                studentLoans = null,
                tollFees = 30.0,
                insurances = mutableListOf(),
                subscriptions = mutableListOf(),
            ),
            consumption = 1500.0,
            savings = 1200.0,
            investments = 600.0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        whenever(personalFinanceRepository.findAll()).thenReturn(listOf(firstFinance, secondFinance))

        val result = personalFinanceService.getTotalOverview()

        assertAll(
            { assertEquals(2, result.size) },
            { assertEquals(firstFinance.id, result.first().id) },
            { assertEquals(firstMonthlyExpense.id, result.first().monthlyExpenses.id) },
            { assertEquals(1, result.first().monthlyExpenses.insurances?.size) },
            { assertEquals("Health Insurance", result.first().monthlyExpenses.insurances?.first()?.insuranceType) },
            { assertEquals(0, result.last().monthlyExpenses.subscriptions?.size) },
            { assertEquals(6500.0, result.last().monthlyIncome) },
        )
    }
}