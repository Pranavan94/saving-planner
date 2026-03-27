package com.finance.saving_planner.controller

import com.fasterxml.jackson.databind.JsonNode
import com.finance.saving_planner.dto.PersonalFinanceOverviewDTO
import com.finance.saving_planner.service.PersonalFinanceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
import org.springframework.web.multipart.MultipartFile
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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun registerPersonalFinance(@RequestBody personalFinance: PersonalFinanceOverviewDTO): String {
        logger.info("POST {} - create personal finance overview", "$BASE_PATH/finance")
        val savedFinance = personalFinanceService.createPersonalFinanceOverview(personalFinance)
        return "Personal Finance Overview with id: ${savedFinance.id} created successfully"
    }

    @GetMapping(PATH_FIND)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun getFinancialOverviewBy(@PathVariable financeId: UUID): PersonalFinanceOverviewDTO {
        logger.info("GET {}/{} - fetch personal finance overview", BASE_PATH, financeId)
        val result = personalFinanceService.getPersonalFinanceOverview(financeId)
        logger.debug("Finance overview result for {}: {}", financeId, result)
        return result
    }

    @PutMapping(PATH_FIND)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun updatePersonalFinanceOverview(@PathVariable financeId: UUID, @RequestBody body: JsonNode): String {
        logger.info("PUT {}/{} - update personal finance overview", BASE_PATH, body)
         personalFinanceService.updatePersonalFinanceOverview(financeId, body)
        return "Personal Finance Overview with id: $financeId updated successfully"
    }

    @DeleteMapping(PATH_FIND)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun deletePersonalFinanceOverview(@PathVariable financeId: UUID) : ResponseEntity<String> {
        logger.info("DELETE {}/{} - delete personal finance overview", BASE_PATH, financeId)
        try {
            val result = personalFinanceService.deletePersonalFinanceOverview(financeId)
            return ResponseEntity.ok(result)
        } catch (e: EntityNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message ?: "Saving Overview not found")
        }
    }

    // all users
    @Operation(
        summary = "Get Financial Overview",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(schema = Schema(implementation = PersonalFinanceOverviewDTO::class))],
            ),
            ApiResponse(responseCode = "404", description = "No overview found"),
        ],
    )
    @GetMapping(PATH_FIND_ALL)
    @PreAuthorize("hasRole('ADMIN')")
    fun getTotalOverview(): Collection<PersonalFinanceOverviewDTO> {
        logger.info("GET {} - fetch all personal finance overviews", BASE_PATH)
        return personalFinanceService.getTotalOverview()
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun uploadFile(@RequestBody file: MultipartFile): ResponseEntity<String> {
        if(file.isEmpty) {
            return ResponseEntity.badRequest().body("File is empty")
        }
        logger.info("POST {} - upload file", BASE_PATH)
        personalFinanceService.processCsv(file.inputStream)
        return ResponseEntity.ok("File uploaded successfully")
    }
}