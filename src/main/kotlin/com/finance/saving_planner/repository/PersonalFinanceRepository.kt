package com.finance.saving_planner.repository

import com.finance.saving_planner.dto.PersonalFinanceOverviewDTO
import com.finance.saving_planner.model.PersonalFinance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PersonalFinanceRepository : JpaRepository<PersonalFinance, UUID> {

    @Query(
        """
        SELECT new com.finance.saving_planner.dto.PersonalFinanceOverviewDTO(
            pf.id,
            pf.startDate,
            pf.endDate,
            pf.monthlyIncome,
            pf.monthlyExpenses,
            pf.consumption,
            pf.savings,
            pf.investments,
            pf.mortgagePayment,
            pf.foodBudget
        )
        FROM PersonalFinance pf
        WHERE pf.id = :financeId
    """,
    )

    fun findByFinanceId(financeId: UUID): PersonalFinanceOverviewDTO?
}