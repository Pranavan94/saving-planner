package com.finance.saving_planner.service.impl

import com.fasterxml.jackson.databind.JsonNode
import com.finance.saving_planner.dto.PersonalFinanceOverviewDTO
import com.finance.saving_planner.model.PersonalFinance
import com.finance.saving_planner.repository.PersonalFinanceRepository
import com.finance.saving_planner.service.PersonalFinanceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID

@Service
class PersonalFinanceServiceImpl (private val personalFinanceRepository: PersonalFinanceRepository) : PersonalFinanceService {
    companion object {
        private val logger = LoggerFactory.getLogger(PersonalFinanceServiceImpl::class.java)
    }

    override fun createPersonalFinanceOverview(personalFinance: PersonalFinance): String {
        logger.info("Creating personal finance overview for period {} to {}", personalFinance.startDate, personalFinance.endDate)
        val savedFinance = personalFinanceRepository.save(personalFinance)
        logger.info("Created personal finance overview with id {}", savedFinance.id)
        return "Personal Finance Overview with id ${savedFinance.id} created successfully"
    }

    override fun getTotalOverview(): Collection<PersonalFinance> {
        logger.debug("Fetching all personal finance overviews")
        return personalFinanceRepository.findAll()
    }

    override fun updatePersonalFinanceOverview(personalFinance: JsonNode): String {
        val financeId = personalFinance["id"].asText()
        logger.info("Updating personal finance overview with id {}", financeId)

        val finance = personalFinanceRepository.findById(UUID.fromString(financeId))
            .orElseThrow { IllegalArgumentException("Personal Finance Overview with ID $financeId not found") }

        val updateFinanceOverview = finance.copy(
            startDate = (personalFinance["startDate"]?.asText() ?: finance.startDate) as Date,
            endDate = (personalFinance["endDate"]?.asText() ?: finance.endDate) as Date,
            monthlyIncome = personalFinance["monthlyIncome"]?.asDouble() ?: finance.monthlyIncome,
            monthlyExpenses = personalFinance["monthlyExpenses"]?.asDouble() ?: finance.monthlyExpenses,
            consumption = personalFinance["consumption"]?.asDouble() ?: finance.consumption,
            savings = personalFinance["savings"]?.asDouble() ?: finance.savings,
            investments = personalFinance["investments"]?.asDouble() ?: finance.investments,
            mortgagePayment = personalFinance["mortgagePayment"]?.asDouble() ?: finance.mortgagePayment,
            foodBudget = personalFinance["foodBudget"]?.asDouble() ?: finance.foodBudget
        )

        personalFinanceRepository.save(updateFinanceOverview)
        logger.info("Updated personal finance overview with id {}", financeId)
        return "Personal Finance Overview with id $financeId successfully updated"
    }

    override fun deletePersonalFinanceOverview(financeId: UUID): String {
        TODO("Not yet implemented")
    }

    override fun getPersonalFinanceOverview(financeId: UUID): PersonalFinanceOverviewDTO {
        logger.info("Fetching personal finance overview with id {}", financeId)
        return personalFinanceRepository.findByFinanceId(financeId)?.also {
            logger.debug("Fetched personal finance overview with id {}", financeId)
        } ?: throw IllegalArgumentException("Personal Finance Overview with ID $financeId not found")
    }
}