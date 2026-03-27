package com.finance.saving_planner.config

import com.finance.saving_planner.model.UserRole
import com.finance.saving_planner.repository.UserRepository
import com.nimbusds.jose.jwk.source.ImmutableSecret
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val userRepository: UserRepository,
    @Value("\${app.cors.allowed-origins:http://localhost:3000}")
    private val allowedOriginsProperty: String,
    @Value("\${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private val allowedMethodsProperty: String,
    @Value("\${app.cors.allowed-headers:*}")
    private val allowedHeadersProperty: String,
    @Value("\${app.cors.allow-credentials:true}")
    private val allowCredentialsProperty: Boolean,
    @Value("\${app.cors.max-age:3600}")
    private val maxAgeProperty: Long,
    @Value("\${app.jwt.secret}")
    private val jwtSecret: String,
    @Value("\${app.jwt.issuer:saving-planner}")
    private val jwtIssuer: String,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
        authenticationConfiguration.authenticationManager

    @Bean
    fun userDetailsService(): UserDetailsService = UserDetailsService { username ->
        val normalizedEmail = username.trim().lowercase()
        val user = userRepository.findByEmailIgnoreCase(normalizedEmail)
            ?: throw UsernameNotFoundException("User with email $normalizedEmail not found")
        val userRole = UserRole.from(user.role)

        User.withUsername(user.email)
            .password(user.passwordHash)
            .authorities(SimpleGrantedAuthority(userRole.authority()))
            .build()
    }

    @Bean
    fun jwtSecretKey(): SecretKey {
        require(jwtSecret.toByteArray().size >= 32) {
            "JWT secret must be at least 32 characters long"
        }

        return SecretKeySpec(jwtSecret.toByteArray(), "HmacSHA256")
    }

    @Bean
    fun jwtEncoder(jwtSecretKey: SecretKey): JwtEncoder = NimbusJwtEncoder(ImmutableSecret(jwtSecretKey))

    @Bean
    fun jwtDecoder(jwtSecretKey: SecretKey): JwtDecoder {
        val decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build()

        val withIssuer: OAuth2TokenValidator<Jwt> = JwtValidators.createDefaultWithIssuer(jwtIssuer)
        decoder.setJwtValidator(DelegatingOAuth2TokenValidator(withIssuer))
        return decoder
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val grantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter().apply {
            setAuthoritiesClaimName("roles")
            setAuthorityPrefix("ROLE_")
        }

        return JwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter)
        }
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity, jwtAuthenticationConverter: JwtAuthenticationConverter): SecurityFilterChain {
        return http
            .cors(withDefaults())
            .csrf { csrf -> csrf.disable() }
            .sessionManagement { sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/users/user").permitAll()
                .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter) }
            }
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = allowedOriginsProperty.toCsvList()
            allowedMethods = allowedMethodsProperty.toCsvList()
            allowedHeaders = allowedHeadersProperty.toCsvList()
            allowCredentials = allowCredentialsProperty
            maxAge = maxAgeProperty
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }

    private fun String.toCsvList(): List<String> =
        split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}
