package com.finance.saving_planner.controller

import com.fasterxml.jackson.databind.JsonNode
import com.finance.saving_planner.dto.AllUserInfoDTO
import com.finance.saving_planner.dto.CreateUserRequest
import com.finance.saving_planner.dto.MessageResponse
import com.finance.saving_planner.model.User
import com.finance.saving_planner.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "registration")
@RestController
@RequestMapping(path = [UserController.BASE_PATH])
class UserController(private val userService: UserService) {

    companion object {
        const val BASE_PATH: String = "/api/v1/users"
        const val PATH_FIND = "/{userId}"
        const val PATH_FIND_ALL = ""
    }

    // register here
    @PostMapping("/user")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUser(@Valid @RequestBody request: CreateUserRequest): MessageResponse {
        return MessageResponse(userService.createUser(request))
    }

    @Operation(
        summary = "Get user by uuid",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(schema = Schema(implementation = User::class))],
            ),
            ApiResponse(responseCode = "404", description = "User not found"),
        ],
    )
    @GetMapping(PATH_FIND)
    fun findUserById(@PathVariable userId: UUID): User {
        val result = userService.getUser(userId)
        println("====>>> findUserById() $result")
        return result
    }

    @GetMapping("/allinfo/{userId}")
    fun getAllUserInfoById(@PathVariable userId: UUID): AllUserInfoDTO {
        val result = userService.getAllUserInfoById(userId)
        println("====>>> getAllUserInfoById() $result")
        return result
    }

    // all users
    @Operation(
        summary = "Get all users",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(schema = Schema(implementation = User::class))],
            ),
            ApiResponse(responseCode = "404", description = "No users found"),
        ],
    )
    @GetMapping(PATH_FIND_ALL)
    fun getAllUsers(): Collection<User> {
        return userService.getAllUsers()
    }

    @PutMapping("/update")
    fun updateUser(@RequestBody body: JsonNode): String {
        val result = userService.updateUser(body)
        println("====>>> updateUser() $result")
        return result
    }

    @DeleteMapping("/remove/{userid}")
    fun removeUser(@PathVariable userid: UUID): String {
        return userService.deleteUser(userid)
    }
}