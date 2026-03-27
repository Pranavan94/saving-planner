package com.finance.saving_planner.service

import com.finance.saving_planner.dto.LoginRequest
import com.finance.saving_planner.dto.TokenResponse

fun interface AuthService {
    fun login(request: LoginRequest): TokenResponse
}


