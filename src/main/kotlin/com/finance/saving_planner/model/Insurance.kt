package com.finance.saving_planner.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "insurance")
data class Insurance (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    val id: UUID? = null,
    val insuranceType: String,
    val insuranceCost: Double,
    val insuranceCompany: String,
) {
    init {
        require(insuranceType.isNotBlank()) { "Insurance name cannot be blank" }
        require(insuranceCost >= 0) { "Insurance cost must be non-negative" }
        require(insuranceCompany.isNotBlank()) { "Insurance company cannot be blank" }
    }
}