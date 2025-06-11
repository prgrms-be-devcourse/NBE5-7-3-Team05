package io.powerrangers.backend.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import io.powerrangers.backend.dto.BaseResponse
import io.powerrangers.backend.dto.TaskResponseDto
import io.powerrangers.backend.dto.UserGetProfileResponseDto
import io.powerrangers.backend.dto.UserUpdateProfileRequestDto
import io.powerrangers.backend.service.ContextUtil
import io.powerrangers.backend.service.CookieFactory
import io.powerrangers.backend.service.UserService
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.time.LocalDate

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @PatchMapping(value = ["/{userId}"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Throws(IOException::class)
    fun updateUserProfile(
        @PathVariable userId: Long?,
        @RequestPart(value = "image", required = false) image: MultipartFile?,
        @RequestPart(value = "dto") request: UserUpdateProfileRequestDto
    ): ResponseEntity<BaseResponse<Void?>?> {
        log.info{"nickname = ${request.nickname}"}
        log.info{"intro = ${request.intro}"}
        userService.updateUserProfile(userId, request, image)
        return BaseResponse.success(HttpStatus.OK)
    }

    @GetMapping("/{userId}")
    fun getUserProfile(@PathVariable userId: Long?): ResponseEntity<BaseResponse<UserGetProfileResponseDto?>?> {
        return BaseResponse.success<UserGetProfileResponseDto?>(HttpStatus.OK, userService!!.getUserProfile(userId))
    }

    @GetMapping
    fun searchUserProfile(@RequestParam nickname: String): ResponseEntity<BaseResponse<MutableList<UserGetProfileResponseDto?>?>?> {
        return BaseResponse.success<MutableList<UserGetProfileResponseDto?>?>(
            HttpStatus.OK,
            userService!!.searchUserProfile(nickname)
        )
    }

    @DeleteMapping("/{userId}")
    fun cancelAccount(@PathVariable userId: Long?): ResponseEntity<BaseResponse<Void?>?> {
        userService!!.cancelAccount(userId)
        return BaseResponse.success(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/{userId}/tasks")
    fun getUserTasks(
        @PathVariable userId: Long?,
        @RequestParam date: LocalDate?
    ): ResponseEntity<BaseResponse<MutableList<TaskResponseDto?>?>?> {
        return BaseResponse.success<MutableList<TaskResponseDto?>?>(
            HttpStatus.OK,
            userService!!.getTasksByUser(userId, date)
        )
    }

    @PostMapping("/logout")
    fun logoutUser(): ResponseEntity<Void?> {
        userService!!.logout()
        val deleteAccessCookie = CookieFactory.deleteAccessCookie()
        val deleteRefreshCookie = CookieFactory.deleteRefreshCookie()

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString())
            .header(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString())
            .build<Void?>()
    }

    @PostMapping("/reissue")
    fun reissueToken(
        @CookieValue(value = CookieFactory.REFRESH_TOKEN) refreshToken: String?
    ): ResponseEntity<String?> {
        val newAccessToken = userService!!.reissueAccessToken(refreshToken)
        val accessCookie = CookieFactory.createAccessCookie(newAccessToken)
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
            .build<String?>()
    }

    @get:GetMapping("/me")
    val currentUserId: ResponseEntity<BaseResponse<Long?>?>
        get() {
            val userId = ContextUtil.getCurrentUserId()
            return BaseResponse.success<Long?>(HttpStatus.OK, userId)
        }
}
