package com.finance.saving_planner.config

import com.finance.saving_planner.model.User
import com.finance.saving_planner.model.UserRole
import com.finance.saving_planner.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class BootstrapAdminConfig(
    @Value("\${app.security.bootstrap-admin.enabled:false}") private val bootstrapAdminEnabled: Boolean,
    @Value("\${app.security.bootstrap-admin.email:}") private val bootstrapAdminEmail: String,
    @Value("\${app.security.bootstrap-admin.password:}") private val bootstrapAdminPassword: String,
    @Value("\${app.security.bootstrap-admin.first-name:System}") private val bootstrapAdminFirstName: String,
    @Value("\${app.security.bootstrap-admin.last-name:Administrator}") private val bootstrapAdminLastName: String,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(BootstrapAdminConfig::class.java)
    }

    @Bean
    fun bootstrapAdminUserRunner(
        userRepository: UserRepository,
        passwordEncoder: PasswordEncoder): CommandLineRunner = CommandLineRunner {
        if (!bootstrapAdminEnabled) {
            logger.info("Bootstrap admin creation disabled")
            return@CommandLineRunner
        }

        val normalizedEmail = bootstrapAdminEmail.trim().lowercase()
        if (normalizedEmail.isBlank() || bootstrapAdminPassword.isBlank()) {
            logger.warn("Bootstrap admin credentials are blank; skipping admin creation")
            return@CommandLineRunner
        }

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            logger.debug("Bootstrap admin user already exists for {}", normalizedEmail)
            return@CommandLineRunner
        }

        val adminUser = User(
            email = normalizedEmail,
            firstName = bootstrapAdminFirstName,
            lastName = bootstrapAdminLastName,
            passwordHash = passwordEncoder.encode(bootstrapAdminPassword),
            role = UserRole.ADMIN.name,
            onboardingDone = true,
        )

        userRepository.save(adminUser)
        logger.info("Created bootstrap admin user for {}", normalizedEmail)
    }
}


