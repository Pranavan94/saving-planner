package com.finance.saving_planner.repository

import com.finance.saving_planner.model.InvestmentGoal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface InvestmentGoalRepository : JpaRepository<InvestmentGoal, UUID>
