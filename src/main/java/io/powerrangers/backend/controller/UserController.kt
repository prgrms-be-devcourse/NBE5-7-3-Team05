package io.powerrangers.backend.controller

import io.powerrangers.backend.dto.BaseResponse
import io.powerrangers.backend.dto.TaskResponseDto
import io.powerrangers.backend.dto.UserGetProfileResponseDto
import io.powerrangers.backend.dto.UserUpdateProfileRequestDto
import io.powerrangers.backend.service.UserService
import io.powerrangers.backend.utils.REFRESH_TOKEN
import io.powerrangers.backend.utils.createAccessCookie
import io.powerrangers.backend.utils.deleteAccessCookie
import io.powerrangers.backend.utils.deleteRefreshCookie
import io.powerrangers.backend.utils.getCurrentUserId
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate


@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @PatchMapping(value = ["/{userId}"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateUserProfile(
        @PathVariable userId: Long,
        @RequestPart(value = "image", required = false) image: MultipartFile?,
        @RequestPart(value = "dto") request: UserUpdateProfileRequestDto
    ): ResponseEntity<BaseResponse.Success<Nothing>> {
        userService.updateUserProfile(userId, request, image)
        return BaseResponse.success(HttpStatus.OK)
    }

    @GetMapping("/{userId}")
    fun getUserProfile(@PathVariable userId: Long): ResponseEntity<BaseResponse.Success<UserGetProfileResponseDto>> {
        return BaseResponse.success(HttpStatus.OK, userService.getUserProfile(userId))
    }

    @GetMapping
    fun searchUserProfile(@RequestParam nickname: String): ResponseEntity<BaseResponse.Success<List<UserGetProfileResponseDto>>> {
        return BaseResponse.success(
            HttpStatus.OK,
            userService.searchUserProfile(nickname)
        )
    }

    @DeleteMapping("/{userId}")
    fun cancelAccount(@PathVariable userId: Long): ResponseEntity<BaseResponse.Success<Nothing>> {
        userService.cancelAccount(userId)
        return BaseResponse.success(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/{userId}/tasks")
    fun getUserTasks(
        @PathVariable userId: Long,
        @RequestParam date: LocalDate
    ): ResponseEntity<BaseResponse.Success<List<TaskResponseDto>>> {
        return BaseResponse.success(
            HttpStatus.OK,
            userService.getTasksByUser(userId, date)
        )
    }

    @PostMapping("/logout")
    fun logoutUser(): ResponseEntity<Nothing> {
        userService.logout()
        val deleteAccessCookie = deleteAccessCookie()
        val deleteRefreshCookie = deleteRefreshCookie()

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString())
            .header(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString())
            .build<Nothing>()
    }

    @PostMapping("/reissue")
    fun reissueToken(
        @CookieValue(value = REFRESH_TOKEN) refreshToken: String
    ): ResponseEntity<String> {
        val newAccessToken = userService.reissueAccessToken(refreshToken)
        val accessCookie = createAccessCookie(newAccessToken)
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
            .build()
    }

    @GetMapping("/me")
    fun getMyId(): ResponseEntity<BaseResponse.Success<Long>> {
        val userId = getCurrentUserId()
        return BaseResponse.success(HttpStatus.OK, userId)
    }

}
