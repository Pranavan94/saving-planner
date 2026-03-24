package com.finance.saving_planner.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "subscription")
data class Subscription (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    val id: UUID? = null,
    val subscriptionName: String,
    val subscriptionCost: Double,
) {
    init {
        require(subscriptionName.isNotBlank()) { "Subscription name cannot be blank" }
        require(subscriptionCost >= 0) { "Subscription cost must be non-negative" }
    }
}