package com.finance.saving_planner.controller

import com.finance.saving_planner.dto.PersonalFinanceOverviewDTO
import com.finance.saving_planner.model.PersonalFinance
import com.finance.saving_planner.service.PersonalFinanceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(path = [PersonalFinanceController.BASE_PATH])
class PersonalFinanceController(private val personalFinanceService: PersonalFinanceService) {
    companion object {
        private val logger = LoggerFactory.getLogger(PersonalFinanceController::class.java)

        const val BASE_PATH: String = "/api/v1/finance/overview"
        const val PATH_FIND = "/{financeId}"
        const val PATH_FIND_ALL = ""
    }

    // register here
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerPersonalFinance(@RequestBody personalFinance: PersonalFinanceOverviewDTO): String {
        logger.info("POST {} - create personal finance overview", "$BASE_PATH/finance")
        val savedFinance = personalFinanceService.createPersonalFinanceOverview(personalFinance)
        return "Personal Finance Overview with id: ${savedFinance.id} created successfully";
    }

    @GetMapping(PATH_FIND)
    fun getFinancialOverviewBy(@PathVariable financeId: UUID): PersonalFinanceOverviewDTO {
        logger.info("GET {}/{} - fetch personal finance overview", BASE_PATH, financeId)
        val result = personalFinanceService.getPersonalFinanceOverview(financeId)
        logger.debug("Finance overview result for {}: {}", financeId, result)
        return result
    }

    // all users
    @Operation(
        summary = "Get Financial Overview",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(schema = Schema(implementation = PersonalFinance::class))],
            ),
            ApiResponse(responseCode = "404", description = "No overview found"),
        ],
    )
    @GetMapping(PATH_FIND_ALL)
    fun getTotalOverview(): Collection<PersonalFinance> {
        logger.info("GET {} - fetch all personal finance overviews", BASE_PATH)
        return personalFinanceService.getTotalOverview()
    }
}