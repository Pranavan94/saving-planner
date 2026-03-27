package com.finance.saving_planner.controller

import com.finance.saving_planner.dto.LoginRequest
import com.finance.saving_planner.dto.TokenResponse
import com.finance.saving_planner.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(AuthController.BASE_PATH)
class AuthController(private val authService: AuthService) {
    companion object {
        const val BASE_PATH = "/api/v1/auth"
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun login(@Valid @RequestBody request: LoginRequest): TokenResponse = authService.login(request)
}

