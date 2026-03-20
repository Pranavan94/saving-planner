package com.finance.saving_planner

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

abstract class PostgreSQLIntegrationTest {

    companion object {
        private val postgresContainer = SavingPlannerPostgreSQLContainer("postgres:16-alpine").apply {
            withDatabaseName("saving_planner_test")
            withUsername("test")
            withPassword("test")
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
        }
    }

    private class SavingPlannerPostgreSQLContainer(imageName: String) :
        PostgreSQLContainer<SavingPlannerPostgreSQLContainer>(imageName)
}

