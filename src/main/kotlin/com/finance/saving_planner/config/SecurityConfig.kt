package com.finance.saving_planner.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
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
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun inMemoryUserDetailsManager(passwordEncoder: PasswordEncoder): InMemoryUserDetailsManager {
        return InMemoryUserDetailsManager(
            User.withUsername("admin")
                .password(passwordEncoder.encode("password"))
                .authorities("read")
                .build()
        )
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .cors(withDefaults())
            .csrf { csrf -> csrf.disable() }
            .authorizeHttpRequests { auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
            }
            .httpBasic(withDefaults())
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
