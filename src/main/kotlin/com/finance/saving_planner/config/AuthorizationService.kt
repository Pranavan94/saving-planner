package com.finance.saving_planner.config

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.util.UUID

@Service("authorizationService")
class AuthorizationService {
    fun isCurrentUser(userId: UUID, authentication: Authentication?): Boolean {
        val jwt = authentication?.principal as? Jwt ?: return false
        val authenticatedUserId = jwt.getClaimAsString("userId") ?: return false
        return authenticatedUserId == userId.toString()
    }
}

