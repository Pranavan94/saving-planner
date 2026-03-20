package com.finance.saving_planner.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "users")
data class User(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Use UUID generation by the database
    @Column(columnDefinition = "UUID") // Explicitly define the column as UUID
    val id: UUID? = null, // Nullable because it's set by the DB

    @Column(columnDefinition = "UUID")
    val companyId: UUID? = null,
    val email: String,
    @NotBlank
    @Size(min = 1, max = 50)
    val firstName: String? = null,
    val middleName: String? = null,
    @NotBlank
    @Size(min = 1, max = 50)
    val lastName: String? = null,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    val passwordHash: String,
    val role: String,
    val telephoneNumber: Long? = null,
    val onboardingDone: Boolean = false,

    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)