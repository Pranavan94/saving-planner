package com.finance.saving_planner.service

import com.finance.saving_planner.dto.InvestmentGoalDTO
import java.util.UUID

interface InvestmentGoalService {

    fun createInvestmentGoal(investmentGoalDto: InvestmentGoalDTO): InvestmentGoalDTO

    fun getAllInvestmentGoals(): Collection<InvestmentGoalDTO>

    fun getInvestmentGoal(goalId: UUID): InvestmentGoalDTO

    fun updateInvestmentGoal(goalId: UUID, investmentGoalDto: InvestmentGoalDTO): InvestmentGoalDTO

    fun deleteInvestmentGoal(goalId: UUID): String
}
