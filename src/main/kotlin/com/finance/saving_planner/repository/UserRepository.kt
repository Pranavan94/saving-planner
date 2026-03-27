package com.finance.saving_planner.repository

import com.finance.saving_planner.dto.AllUserInfoDTO
import com.finance.saving_planner.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {

    fun findByEmailIgnoreCase(email: String): User?

    fun existsByEmailIgnoreCase(email: String): Boolean

    @Query(
        """
        SELECT new com.finance.saving_planner.dto.AllUserInfoDTO(
            u.id,
            u.email,
            u.firstName,
            u.middleName,
            u.lastName,
            u.role,
            u.telephoneNumber,
            u.onboardingDone
        )
        FROM User u
        WHERE u.id = :userId
    """,
    )
    fun getAllUserInfoDTO(@Param("userId") userId: UUID): AllUserInfoDTO?
}

