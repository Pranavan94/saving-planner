package com.finance.saving_planner.service

import com.finance.saving_planner.dto.SavingsGoalDTO
import java.util.UUID

interface SavingsGoalService {

    fun createSavingsGoal(savingsGoalDto: SavingsGoalDTO): SavingsGoalDTO

    fun getAllSavingsGoals(): Collection<SavingsGoalDTO>

    fun getSavingsGoal(goalId: UUID): SavingsGoalDTO

    fun updateSavingsGoal(goalId: UUID, savingsGoalDto: SavingsGoalDTO): SavingsGoalDTO

    fun deleteSavingsGoal(goalId: UUID): String
}
