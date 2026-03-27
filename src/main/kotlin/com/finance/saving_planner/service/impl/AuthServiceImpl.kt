package com.finance.saving_planner.service.impl

import com.finance.saving_planner.dto.LoginRequest
import com.finance.saving_planner.dto.TokenResponse
import com.finance.saving_planner.model.UserRole
import com.finance.saving_planner.repository.UserRepository
import com.finance.saving_planner.service.AuthService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class AuthServiceImpl(
    private val authenticationManager: AuthenticationManager,
    private val jwtEncoder: JwtEncoder,
    private val userRepository: UserRepository,
    @Value("\${app.jwt.issuer:saving-planner}") private val jwtIssuer: String,
    @Value("\${app.jwt.access-token-ttl:PT1H}") private val accessTokenTtl: Duration, ) : AuthService {
    override fun login(request: LoginRequest): TokenResponse {
        val normalizedEmail = request.email.trim().lowercase()

        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(normalizedEmail, request.password),
        )

        val user = userRepository.findByEmailIgnoreCase(normalizedEmail)
            ?: throw IllegalArgumentException("User with email $normalizedEmail not found")

        val userId = requireNotNull(user.id) { "Authenticated user id is missing" }
        val userRole = UserRole.from(user.role)
        val now = Instant.now()
        val expiresAt = now.plus(accessTokenTtl)

        val claims = JwtClaimsSet.builder()
            .issuer(jwtIssuer)
            .issuedAt(now)
            .expiresAt(expiresAt)
            .subject(user.email)
            .claim("userId", userId.toString())
            .claim("roles", listOf(userRole.name))
            .build()

        val jwt = jwtEncoder.encode(
            JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims,
            ),
        )

        return TokenResponse(
            accessToken = jwt.tokenValue,
            expiresIn = accessTokenTtl.seconds,
            userId = userId,
            email = user.email,
            role = userRole.name,
        )
    }
}

