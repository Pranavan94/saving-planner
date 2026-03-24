package com.finance.saving_planner.service

import com.fasterxml.jackson.databind.JsonNode
import com.finance.saving_planner.dto.PersonalFinanceOverviewDTO
import com.finance.saving_planner.model.PersonalFinance
import java.util.UUID

interface PersonalFinanceService {

    fun createPersonalFinanceOverview(personalFinanceDto: PersonalFinanceOverviewDTO): PersonalFinance

    fun getTotalOverview(): Collection<PersonalFinanceOverviewDTO>

    fun updatePersonalFinanceOverview(financeId: UUID, personalFinance: JsonNode): String

    fun deletePersonalFinanceOverview(financeId: UUID): String

    fun getPersonalFinanceOverview(financeId: UUID): PersonalFinanceOverviewDTO
}