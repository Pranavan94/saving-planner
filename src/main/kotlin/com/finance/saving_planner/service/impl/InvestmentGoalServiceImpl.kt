package com.finance.saving_planner.service.impl

import com.finance.saving_planner.dto.InvestmentGoalDTO
import com.finance.saving_planner.model.InvestmentGoal
import com.finance.saving_planner.repository.InvestmentGoalRepository
import com.finance.saving_planner.service.GoalProgressCalculator
import com.finance.saving_planner.service.InvestmentGoalService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

@Service
class InvestmentGoalServiceImpl(
    private val investmentGoalRepository: InvestmentGoalRepository,
    private val goalProgressCalculator: GoalProgressCalculator,
) : InvestmentGoalService {

    companion object {
        private val logger = LoggerFactory.getLogger(InvestmentGoalServiceImpl::class.java)
    }

    override fun createInvestmentGoal(investmentGoalDto: InvestmentGoalDTO): InvestmentGoalDTO {
        logger.info("Creating investment goal for purpose '{}'", investmentGoalDto.purpose)
        validate(investmentGoalDto)

        val entity = InvestmentGoal(
            purpose = investmentGoalDto.purpose.trim(),
            targetAmount = investmentGoalDto.targetAmount,
            startDate = investmentGoalDto.startDate,
            targetDate = investmentGoalDto.targetDate,
            startingAmount = investmentGoalDto.startingAmount,
            expectedAnnualReturnRate = investmentGoalDto.expectedAnnualReturnRate,
        )
        val saved = investmentGoalRepository.save(entity)
        logger.info("Created investment goal with id {}", saved.id)
        return toDto(saved)
    }

    @Transactional(readOnly = true)
    override fun getAllInvestmentGoals(): Collection<InvestmentGoalDTO> {
        logger.debug("Fetching all investment goals")
        return investmentGoalRepository.findAll().map(::toDto)
    }

    @Transactional(readOnly = true)
    override fun getInvestmentGoal(goalId: UUID): InvestmentGoalDTO {
        logger.info("Fetching investment goal with id {}", goalId)
        return toDto(findOrThrow(goalId))
    }

    override fun updateInvestmentGoal(goalId: UUID, investmentGoalDto: InvestmentGoalDTO): InvestmentGoalDTO {
        logger.info("Updating investment goal with id {}", goalId)
        validate(investmentGoalDto)
        val existing = findOrThrow(goalId)

        val updated = existing.copy(
            purpose = investmentGoalDto.purpose.trim(),
            targetAmount = investmentGoalDto.targetAmount,
            startDate = investmentGoalDto.startDate,
            targetDate = investmentGoalDto.targetDate,
            startingAmount = investmentGoalDto.startingAmount,
            expectedAnnualReturnRate = investmentGoalDto.expectedAnnualReturnRate,
            updatedAt = LocalDateTime.now(),
        )
        val saved = investmentGoalRepository.save(updated)
        logger.info("Updated investment goal with id {}", saved.id)
        return toDto(saved)
    }

    override fun deleteInvestmentGoal(goalId: UUID): String {
        logger.info("Deleting investment goal with id {}", goalId)
        require(investmentGoalRepository.existsById(goalId)) {
            "Investment Goal with ID $goalId not found"
        }
        investmentGoalRepository.deleteById(goalId)
        return "Investment Goal with id $goalId successfully deleted"
    }

    private fun findOrThrow(goalId: UUID): InvestmentGoal =
        investmentGoalRepository.findById(goalId).orElseThrow {
            IllegalArgumentException("Investment Goal with ID $goalId not found")
        }

    private fun validate(dto: InvestmentGoalDTO) {
        require(dto.purpose.isNotBlank()) { "Purpose is required" }
        require(dto.targetAmount > 0) { "Target amount must be greater than zero" }
        require(dto.startingAmount >= 0) { "Starting amount cannot be negative" }
        require(dto.expectedAnnualReturnRate >= 0) { "Expected annual return rate cannot be negative" }
        validateDateRange(dto.startDate, dto.targetDate)
    }

    private fun validateDateRange(startDate: Date?, targetDate: Date?) {
        if (startDate != null && targetDate != null) {
            require(!targetDate.before(startDate)) { "targetDate must be greater than or equal to startDate" }
        }
    }

    private fun toDto(goal: InvestmentGoal): InvestmentGoalDTO {
        val progress = goalProgressCalculator.calculate(goal.startDate, goal.targetDate) { it.investments }
        return InvestmentGoalDTO(
            id = goal.id,
            purpose = goal.purpose,
            targetAmount = goal.targetAmount,
            startDate = goal.startDate,
            targetDate = goal.targetDate,
            startingAmount = goal.startingAmount,
            expectedAnnualReturnRate = goal.expectedAnnualReturnRate,
            currentAmount = goal.startingAmount + progress.contributed,
            averageMonthlyContribution = progress.averageMonthlyContribution,
        )
    }
}
