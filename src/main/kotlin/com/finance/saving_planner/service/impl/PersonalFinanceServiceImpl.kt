package com.finance.saving_planner.service.impl

import com.fasterxml.jackson.databind.JsonNode
import com.finance.saving_planner.dto.PersonalFinanceOverviewDTO
import com.finance.saving_planner.model.PersonalFinance
import com.finance.saving_planner.repository.PersonalFinanceRepository
import com.finance.saving_planner.service.PersonalFinanceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date
import java.util.UUID

@Service
class PersonalFinanceServiceImpl(private val personalFinanceRepository: PersonalFinanceRepository) : PersonalFinanceService {
    companion object {
        private val logger = LoggerFactory.getLogger(PersonalFinanceServiceImpl::class.java)
    }

    override fun createPersonalFinanceOverview(personalFinanceDto: PersonalFinanceOverviewDTO): PersonalFinance {
        logger.info("Creating personal finance overview for period {} to {}", personalFinanceDto.startDate, personalFinanceDto.endDate)
        val personalFinanceEntity = PersonalFinance(
            startDate = personalFinanceDto.startDate,
            endDate = personalFinanceDto.endDate,
            monthlyIncome = personalFinanceDto.monthlyIncome,
            monthlyExpenses = personalFinanceDto.monthlyExpenses,
            consumption = personalFinanceDto.consumption,
            savings = personalFinanceDto.savings,
            investments = personalFinanceDto.investments,
            mortgagePayment = personalFinanceDto.mortgagePayment,
            foodBudget = personalFinanceDto.foodBudget,
        )
        val savedPersonalFinance = personalFinanceRepository.save(personalFinanceEntity)
        logger.info("Created personal finance overview with id {}", savedPersonalFinance.id)
        return savedPersonalFinance
    }

    override fun getTotalOverview(): Collection<PersonalFinance> {
        logger.debug("Fetching all personal finance overviews")
        return personalFinanceRepository.findAll()
    }

    override fun updatePersonalFinanceOverview(financeId: UUID, personalFinance: JsonNode): String {
        val finance = personalFinanceRepository.findById(financeId).orElseThrow {
            throw IllegalArgumentException("Personal Finance Overview id is required")
        }

        val updatedFinanceOverview = finance.copy(
            startDate = parseDate(personalFinance, "startDate", finance.startDate),
            endDate = parseDate(personalFinance, "endDate", finance.endDate),
            monthlyIncome = personalFinance["monthlyIncome"]?.asDouble() ?: finance.monthlyIncome,
            monthlyExpenses = personalFinance["monthlyExpenses"]?.asDouble() ?: finance.monthlyExpenses,
            consumption = personalFinance["consumption"]?.asDouble() ?: finance.consumption,
            savings = personalFinance["savings"]?.asDouble() ?: finance.savings,
            investments = personalFinance["investments"]?.asDouble() ?: finance.investments,
            mortgagePayment = personalFinance["mortgagePayment"]?.asDouble() ?: finance.mortgagePayment,
            foodBudget = personalFinance["foodBudget"]?.asDouble() ?: finance.foodBudget,
        )

        val savedFinanceOverview = personalFinanceRepository.save(updatedFinanceOverview)
        logger.info("Updated personal finance overview with id {}", financeId)
        return "Personal Finance Overview with id ${savedFinanceOverview.id} successfully updated"
    }

    override fun deletePersonalFinanceOverview(financeId: UUID): String {
        logger.info("Deleting personal finance overview with id {}", financeId)
        if (!personalFinanceRepository.existsById(financeId)) {
            throw IllegalArgumentException("Personal Finance Overview with ID $financeId not found")
        }

        personalFinanceRepository.deleteById(financeId)
        logger.info("Deleted personal finance overview with id {}", financeId)
        return "Personal Finance Overview with id $financeId successfully deleted"
    }

    override fun getPersonalFinanceOverview(financeId: UUID): PersonalFinanceOverviewDTO {
        logger.info("Fetching personal finance overview with id {}", financeId)
        return personalFinanceRepository.findByFinanceId(financeId)?.also {
            logger.debug("Fetched personal finance overview with id {}", financeId)
        } ?: throw IllegalArgumentException("Personal Finance Overview with ID $financeId not found")
    }

    private fun parseDate(personalFinance: JsonNode, fieldName: String, currentValue: Date?): Date? {
        val fieldValue = personalFinance[fieldName] ?: return currentValue

        if (fieldValue.isNull) {
            return null
        }

        return try {
            when {
                fieldValue.isNumber -> Date(fieldValue.asLong())
                else -> Date.from(Instant.parse(fieldValue.asText()))
            }
        } catch (exception: Exception) {
            throw IllegalArgumentException(
                "Invalid date format for field '$fieldName'. Use ISO-8601 or epoch milliseconds",
                exception,
            )
        }
    }
}