package com.finance.saving_planner.service

import com.fasterxml.jackson.databind.JsonNode
import com.finance.saving_planner.dto.AllUserInfoDTO
import com.finance.saving_planner.dto.CreateUserRequest
import com.finance.saving_planner.model.User
import java.util.UUID

interface UserService {

    fun createUser(request: CreateUserRequest): String

    fun getUser(userId: UUID): User

    fun getAllUsers(): Collection<User>

    fun updateUser(user: JsonNode): String

    fun deleteUser(userId: UUID): String

    fun getAllUserInfoById(userId: UUID): AllUserInfoDTO
}