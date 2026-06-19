package com.finance.saving_planner.service.impl

import com.finance.saving_planner.dto.SavingsGoalDTO
import com.finance.saving_planner.model.SavingsGoal
import com.finance.saving_planner.service.GoalAllocationSupport
import com.finance.saving_planner.repository.SavingsGoalRepository
import com.finance.saving_planner.service.GoalProgressCalculator
import com.finance.saving_planner.service.SavingsGoalService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

@Service
class SavingsGoalServiceImpl(
    private val savingsGoalRepository: SavingsGoalRepository,
    private val goalProgressCalculator: GoalProgressCalculator,
) : SavingsGoalService {

    companion object {
        private val logger = LoggerFactory.getLogger(SavingsGoalServiceImpl::class.java)
    }

    override fun createSavingsGoal(savingsGoalDto: SavingsGoalDTO): SavingsGoalDTO {
        logger.info("Creating savings goal for purpose '{}'", savingsGoalDto.purpose)
        validate(savingsGoalDto)

        val entity = SavingsGoal(
            purpose = savingsGoalDto.purpose.trim(),
            targetAmount = savingsGoalDto.targetAmount,
            startDate = savingsGoalDto.startDate,
            targetDate = savingsGoalDto.targetDate,
            startingAmount = savingsGoalDto.startingAmount,
            defaultAllocationPercentage = savingsGoalDto.defaultAllocationPercentage,
            monthlyAllocationOverrides = GoalAllocationSupport.toModel(savingsGoalDto.monthlyAllocationOverrides),
        )
        val saved = savingsGoalRepository.save(entity)
        logger.info("Created savings goal with id {}", saved.id)
        return toDto(saved)
    }

    @Transactional(readOnly = true)
    override fun getAllSavingsGoals(): Collection<SavingsGoalDTO> {
        logger.debug("Fetching all savings goals")
        return savingsGoalRepository.findAll().map(::toDto)
    }

    @Transactional(readOnly = true)
    override fun getSavingsGoal(goalId: UUID): SavingsGoalDTO {
        logger.info("Fetching savings goal with id {}", goalId)
        return toDto(findOrThrow(goalId))
    }

    override fun updateSavingsGoal(goalId: UUID, savingsGoalDto: SavingsGoalDTO): SavingsGoalDTO {
        logger.info("Updating savings goal with id {}", goalId)
        validate(savingsGoalDto)
        val existing = findOrThrow(goalId)

        val updated = existing.copy(
            purpose = savingsGoalDto.purpose.trim(),
            targetAmount = savingsGoalDto.targetAmount,
            startDate = savingsGoalDto.startDate,
            targetDate = savingsGoalDto.targetDate,
            startingAmount = savingsGoalDto.startingAmount,
            defaultAllocationPercentage = savingsGoalDto.defaultAllocationPercentage,
            monthlyAllocationOverrides = GoalAllocationSupport.toModel(savingsGoalDto.monthlyAllocationOverrides),
            updatedAt = LocalDateTime.now(),
        )
        val saved = savingsGoalRepository.save(updated)
        logger.info("Updated savings goal with id {}", saved.id)
        return toDto(saved)
    }

    override fun deleteSavingsGoal(goalId: UUID): String {
        logger.info("Deleting savings goal with id {}", goalId)
        require(savingsGoalRepository.existsById(goalId)) {
            "Savings Goal with ID $goalId not found"
        }
        savingsGoalRepository.deleteById(goalId)
        return "Savings Goal with id $goalId successfully deleted"
    }

    private fun findOrThrow(goalId: UUID): SavingsGoal =
        savingsGoalRepository.findById(goalId).orElseThrow {
            IllegalArgumentException("Savings Goal with ID $goalId not found")
        }

    private fun validate(dto: SavingsGoalDTO) {
        require(dto.purpose.isNotBlank()) { "Purpose is required" }
        require(dto.targetAmount > 0) { "Target amount must be greater than zero" }
        require(dto.startingAmount >= 0) { "Starting amount cannot be negative" }
        validateDateRange(dto.startDate, dto.targetDate)
        GoalAllocationSupport.validatePlan(
            defaultAllocationPercentage = dto.defaultAllocationPercentage,
            monthlyAllocationOverrides = dto.monthlyAllocationOverrides,
            startDate = dto.startDate,
            targetDate = dto.targetDate,
        )
    }

    private fun validateDateRange(startDate: Date?, targetDate: Date?) {
        if (startDate != null && targetDate != null) {
            require(!targetDate.before(startDate)) { "targetDate must be greater than or equal to startDate" }
        }
    }

    private fun toDto(goal: SavingsGoal): SavingsGoalDTO {
        val progress = goalProgressCalculator.calculate(
            startDate = goal.startDate,
            targetDate = goal.targetDate,
            defaultAllocationPercentage = goal.defaultAllocationPercentage,
            monthlyAllocationOverrides = goal.monthlyAllocationOverrides,
        ) { it.savings }
        return SavingsGoalDTO(
            id = goal.id,
            purpose = goal.purpose,
            targetAmount = goal.targetAmount,
            startDate = goal.startDate,
            targetDate = goal.targetDate,
            startingAmount = goal.startingAmount,
            defaultAllocationPercentage = goal.defaultAllocationPercentage,
            monthlyAllocationOverrides = GoalAllocationSupport.toDto(goal.monthlyAllocationOverrides),
            currentAmount = goal.startingAmount + progress.contributed,
            averageMonthlyContribution = progress.averageMonthlyContribution,
        )
    }
}
