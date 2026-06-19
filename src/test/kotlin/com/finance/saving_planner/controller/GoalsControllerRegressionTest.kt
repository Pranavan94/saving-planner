package com.finance.saving_planner.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.finance.saving_planner.PostgreSQLIntegrationTest
import com.finance.saving_planner.model.MonthlyExpenses
import com.finance.saving_planner.model.PersonalFinance
import com.finance.saving_planner.repository.InvestmentGoalRepository
import com.finance.saving_planner.repository.PersonalFinanceRepository
import com.finance.saving_planner.repository.SavingsGoalRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.Date

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "planner.user@example.com", roles = ["USER"])
@DisplayName("GoalsController Integration/Regression Tests")
class GoalsControllerRegressionTest : PostgreSQLIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var savingsGoalRepository: SavingsGoalRepository

    @Autowired
    private lateinit var investmentGoalRepository: InvestmentGoalRepository

    @Autowired
    private lateinit var personalFinanceRepository: PersonalFinanceRepository

    @BeforeEach
    fun setUp() {
        savingsGoalRepository.deleteAll()
        investmentGoalRepository.deleteAll()
        personalFinanceRepository.deleteAll()

        personalFinanceRepository.saveAll(
            listOf(
                finance("2026-03-01T00:00:00Z", savings = 1000.0, investments = 500.0),
                finance("2026-04-01T00:00:00Z", savings = 800.0, investments = 400.0),
            ),
        )
    }

    @Test
    fun savingsGoalPersistsMonthlyPlanningAndReturnsAllocationAwareProgress() {
        val payload = objectMapper.createObjectNode().apply {
            put("purpose", "Vacation")
            put("targetAmount", 5000.0)
            put("startDate", "2026-03-01T00:00:00.000+00:00")
            put("targetDate", "2026-04-30T00:00:00.000+00:00")
            put("startingAmount", 100.0)
            put("defaultAllocationPercentage", 50.0)
            putArray("monthlyAllocationOverrides").apply {
                addObject().put("month", "2026-03").put("overridePercentage", 60.0)
                addObject().put("month", "2026-04").put("allocatedAmount", 300.0)
            }
        }

        val createResponse = mockMvc.perform(
            post("/api/v1/goals/savings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.defaultAllocationPercentage").value(50.0))
            .andExpect(jsonPath("$.monthlyAllocationOverrides.length()").value(2))
            .andExpect(jsonPath("$.monthlyAllocationOverrides[0].month").value("2026-03"))
            .andExpect(jsonPath("$.currentAmount").value(1000.0))
            .andExpect(jsonPath("$.averageMonthlyContribution").value(450.0))
            .andReturn()

        val createdId = objectMapper.readTree(createResponse.response.contentAsString).path("id").asText()

        mockMvc.perform(get("/api/v1/goals/savings/{goalId}", createdId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.defaultAllocationPercentage").value(50.0))
            .andExpect(jsonPath("$.monthlyAllocationOverrides[1].allocatedAmount").value(300.0))
    }

    @Test
    fun investmentGoalPersistsMonthlyPlanningAndReturnsAllocationAwareProgress() {
        val payload = objectMapper.createObjectNode().apply {
            put("purpose", "Retirement")
            put("targetAmount", 100000.0)
            put("startDate", "2026-03-01T00:00:00.000+00:00")
            put("targetDate", "2026-04-30T00:00:00.000+00:00")
            put("startingAmount", 1000.0)
            put("expectedAnnualReturnRate", 8.0)
            put("defaultAllocationPercentage", 25.0)
            putArray("monthlyAllocationOverrides").apply {
                addObject().put("month", "2026-04").put("overridePercentage", 50.0)
            }
        }

        val createResponse = mockMvc.perform(
            post("/api/v1/goals/investments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.expectedAnnualReturnRate").value(8.0))
            .andExpect(jsonPath("$.defaultAllocationPercentage").value(25.0))
            .andExpect(jsonPath("$.monthlyAllocationOverrides.length()").value(1))
            .andExpect(jsonPath("$.currentAmount").value(1325.0))
            .andExpect(jsonPath("$.averageMonthlyContribution").value(162.5))
            .andReturn()

        val createdId = objectMapper.readTree(createResponse.response.contentAsString).path("id").asText()

        mockMvc.perform(get("/api/v1/goals/investments/{goalId}", createdId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.monthlyAllocationOverrides[0].month").value("2026-04"))
            .andExpect(jsonPath("$.monthlyAllocationOverrides[0].overridePercentage").value(50.0))
    }

    private fun finance(instant: String, savings: Double, investments: Double): PersonalFinance =
        PersonalFinance(
            startDate = date(instant),
            endDate = date(instant),
            monthlyIncome = 5000.0,
            monthlyExpenses = MonthlyExpenses(
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
            ),
            savings = savings,
            investments = investments,
        )

    private fun date(instant: String): Date = Date.from(Instant.parse(instant))
}

