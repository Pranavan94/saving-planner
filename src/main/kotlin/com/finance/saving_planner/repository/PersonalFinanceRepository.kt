package com.finance.saving_planner.repository

import com.finance.saving_planner.model.PersonalFinance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PersonalFinanceRepository : JpaRepository<PersonalFinance, UUID> {

    @Query(
        """
        SELECT pf
        FROM PersonalFinance pf
        LEFT JOIN FETCH pf.monthlyExpenses me
        WHERE pf.id = :financeId
    """,
    )

    fun findByFinanceId(@Param("financeId") financeId: UUID): PersonalFinance?
}