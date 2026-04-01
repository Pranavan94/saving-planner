package com.finance.saving_planner.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "monthly_expenses")
data class MonthlyExpenses (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    val id: UUID? = null,
    val mortgagePayment: Double,
    val sharedHouseCost: Double,
    val foodBudget: Double,
    val carLoan: Double?,
    val creditCardBill: Double?,
    val electricityBill: Double?,
    val studentLoans: Double?,
    val tollFees: Double?,
    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val insurances:  MutableList<Insurance>,
    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val subscriptions: MutableList<Subscription>,
) {
    fun getSumOfMonthlyExpenses() : Double {
        return this.mortgagePayment + this.sharedHouseCost + this.foodBudget + (this.carLoan ?: 0.0) + (this.creditCardBill ?: 0.0) +
                (this.electricityBill ?: 0.0) + (this.studentLoans ?: 0.0) + (this.tollFees ?: 0.0) +
                this.insurances.sumOf { it.insuranceCost } + this.subscriptions.sumOf { it.subscriptionCost }
    }
}