package com.finance.saving_planner.controller

import com.finance.saving_planner.dto.InvestmentGoalDTO
import com.finance.saving_planner.service.InvestmentGoalService
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
@RequestMapping(path = [InvestmentGoalController.BASE_PATH])
class InvestmentGoalController(private val investmentGoalService: InvestmentGoalService) {
    companion object {
        private val logger = LoggerFactory.getLogger(InvestmentGoalController::class.java)
        const val BASE_PATH: String = "/api/v1/goals/investments"
        const val PATH_FIND = "/{goalId}"
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun createInvestmentGoal(@RequestBody investmentGoal: InvestmentGoalDTO): InvestmentGoalDTO {
        logger.info("POST {} - create investment goal", BASE_PATH)
        return investmentGoalService.createInvestmentGoal(investmentGoal)
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun getAllInvestmentGoals(): Collection<InvestmentGoalDTO> {
        logger.info("GET {} - fetch all investment goals", BASE_PATH)
        return investmentGoalService.getAllInvestmentGoals()
    }

    @GetMapping(PATH_FIND)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun getInvestmentGoal(@PathVariable goalId: UUID): InvestmentGoalDTO {
        logger.info("GET {}/{} - fetch investment goal", BASE_PATH, goalId)
        return investmentGoalService.getInvestmentGoal(goalId)
    }

    @PutMapping(PATH_FIND)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun updateInvestmentGoal(@PathVariable goalId: UUID, @RequestBody investmentGoal: InvestmentGoalDTO): InvestmentGoalDTO {
        logger.info("PUT {}/{} - update investment goal", BASE_PATH, goalId)
        return investmentGoalService.updateInvestmentGoal(goalId, investmentGoal)
    }

    @DeleteMapping(PATH_FIND)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun deleteInvestmentGoal(@PathVariable goalId: UUID): String {
        logger.info("DELETE {}/{} - delete investment goal", BASE_PATH, goalId)
        return investmentGoalService.deleteInvestmentGoal(goalId)
    }
}
