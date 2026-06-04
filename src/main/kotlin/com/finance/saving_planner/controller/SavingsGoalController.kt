package com.finance.saving_planner.controller

import com.finance.saving_planner.dto.SavingsGoalDTO
import com.finance.saving_planner.service.SavingsGoalService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(path = [SavingsGoalController.BASE_PATH])
class SavingsGoalController(private val savingsGoalService: SavingsGoalService) {
    companion object {
        private val logger = LoggerFactory.getLogger(SavingsGoalController::class.java)
        const val BASE_PATH: String = "/api/v1/goals/savings"
        const val PATH_FIND = "/{goalId}"
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun createSavingsGoal(@RequestBody savingsGoal: SavingsGoalDTO): SavingsGoalDTO {
        logger.info("POST {} - create savings goal", BASE_PATH)
        return savingsGoalService.createSavingsGoal(savingsGoal)
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun getAllSavingsGoals(): Collection<SavingsGoalDTO> {
        logger.info("GET {} - fetch all savings goals", BASE_PATH)
        return savingsGoalService.getAllSavingsGoals()
    }

    @GetMapping(PATH_FIND)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun getSavingsGoal(@PathVariable goalId: UUID): SavingsGoalDTO {
        logger.info("GET {}/{} - fetch savings goal", BASE_PATH, goalId)
        return savingsGoalService.getSavingsGoal(goalId)
    }

    @PutMapping(PATH_FIND)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun updateSavingsGoal(@PathVariable goalId: UUID, @RequestBody savingsGoal: SavingsGoalDTO): SavingsGoalDTO {
        logger.info("PUT {}/{} - update savings goal", BASE_PATH, goalId)
        return savingsGoalService.updateSavingsGoal(goalId, savingsGoal)
    }

    @DeleteMapping(PATH_FIND)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun deleteSavingsGoal(@PathVariable goalId: UUID): String {
        logger.info("DELETE {}/{} - delete savings goal", BASE_PATH, goalId)
        return savingsGoalService.deleteSavingsGoal(goalId)
    }
}
