package com.finance.saving_planner.model

enum class UserRole {
    ADMIN,
    USER,
    ;

    fun authority(): String = "ROLE_$name"

    companion object {
        fun from(value: String): UserRole {
            val normalized = value.trim()
            require(normalized.isNotBlank()) { "Role is required" }

            return entries.firstOrNull { it.name.equals(normalized, ignoreCase = true) }
                ?: throw IllegalArgumentException(
                    "Unsupported role '$value'. Allowed roles: ${entries.joinToString { it.name }}",
                )
        }
    }
}

