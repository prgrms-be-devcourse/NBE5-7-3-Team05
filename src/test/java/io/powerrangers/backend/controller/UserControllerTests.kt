package io.powerrangers.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockkStatic
import io.powerrangers.backend.config.JwtAuthenticationFilter
import io.powerrangers.backend.dto.*
import io.powerrangers.backend.exception.AuthTokenException
import io.powerrangers.backend.exception.CustomException
import io.powerrangers.backend.exception.ErrorCode
import io.powerrangers.backend.service.CustomOauth2UserService
import io.powerrangers.backend.service.JwtProvider
import io.powerrangers.backend.service.UserService
import io.powerrangers.backend.utils.getCurrentUserId
import jakarta.servlet.http.Cookie
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.Test


@WebMvcTest(
    controllers = [UserController::class],
    excludeFilters = [
        ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [JwtAuthenticationFilter::class])
    ]
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var userService: UserService

    @MockitoBean
    lateinit var jwtProvider: JwtProvider

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var customOauth2UserService: CustomOauth2UserService

    @Test
    fun `PATCH - 이미지와 DTO 모두 전달하면 200 OK`() {
        val dto = UserUpdateProfileRequestDto(
            nickname = "new_nick",
            intro = "자기소개입니다",
            profileImage = null
        )
        val json = objectMapper.writeValueAsString(dto)
        val dtoPart = MockMultipartFile("dto", "", "application/json", json.toByteArray())
        val image = MockMultipartFile("image", "profile.jpg", "image/jpeg", "image-bytes".toByteArray())

        doNothing().`when`(userService).updateUserProfile(1L, dto, image)

        mockMvc.perform(
            multipart(HttpMethod.PATCH, "/users/1")
                .file(dtoPart)
                .file(image)
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isOk)
    }

    @Test
    fun `PATCH - DTO만 전달하면 200 OK`() {
        val dto = UserUpdateProfileRequestDto("no_image", "소개입니다", null)
        val json = objectMapper.writeValueAsString(dto)
        val dtoPart = MockMultipartFile("dto", "", "application/json", json.toByteArray())

        doNothing().`when`(userService).updateUserProfile(1L, dto, null)

        mockMvc.perform(
            multipart(HttpMethod.PATCH, "/users/1")
                .file(dtoPart)
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isOk)
    }

    @Test
    fun `PATCH - DTO 누락 시 500 INTERNAL_SERVER_ERROR`() {
        val image = MockMultipartFile("image", "profile.jpg", "image/jpeg", "image".toByteArray())

        mockMvc.perform(
            multipart(HttpMethod.PATCH, "/users/1")
                .file(image)
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isInternalServerError)
    }

    @Test
    fun `GET - 특정 userId 프로필 조회 성공 시 200 OK`() {
        val userId = 1L
        val responseDto = UserGetProfileResponseDto(
            userId = userId,
            nickname = "testuser",
            intro = "소개입니다",
            profileImage = "img.jpg"
        )

        doReturn(responseDto).`when`(userService).getUserProfile(userId)

        mockMvc.perform(get("/users/{userId}", userId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.userId").value(userId))
            .andExpect(jsonPath("$.data.nickname").value("testuser"))
    }

    @Test
    fun `GET - 존재하지 않는 userId 조회 시 404 NOT_FOUND`() {
        val userId = 999L
        doThrow(CustomException(ErrorCode.USER_NOT_FOUND))
            .`when`(userService).getUserProfile(userId)

        mockMvc.perform(get("/users/{userId}", userId))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `GET - 닉네임 키워드로 검색 성공 시 200 OK + 유저 리스트 반환`() {
        val nicknameKeyword = "test"
        val users = listOf(
            UserGetProfileResponseDto(1L, "testuser1", "소개1", "img1.jpg"),
            UserGetProfileResponseDto(2L, "testuser2", "소개2", "img2.jpg")
        )

        doReturn(users).`when`(userService).searchUserProfile(nicknameKeyword)

        mockMvc.perform(get("/users").param("nickname", nicknameKeyword))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.size()").value(2))
            .andExpect(jsonPath("$.data[0].nickname").value("testuser1"))
    }

    @Test
    fun `GET - 닉네임 키워드로 검색 결과 없음 시 빈 리스트 반환`() {
        val nicknameKeyword = "nonexistent"
        doReturn(emptyList<UserGetProfileResponseDto>())
            .`when`(userService).searchUserProfile(nicknameKeyword)

        mockMvc.perform(get("/users").param("nickname", nicknameKeyword))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data").isEmpty)
    }

    @Test
    fun `DELETE - 정상적인 userId로 탈퇴 시 204 NO_CONTENT`() {
        val userId = 1L

        doNothing().`when`(userService).cancelAccount(userId)

        mockMvc.perform(delete("/users/{userId}", userId))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `DELETE - 존재하지 않는 userId로 탈퇴 시 404 NOT_FOUND`() {
        val userId = 999L

        doThrow(CustomException(ErrorCode.USER_NOT_FOUND))
            .`when`(userService).cancelAccount(userId)

        mockMvc.perform(delete("/users/{userId}", userId))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `GET - 유효한 날짜와 유저 ID로 할 일 조회 시 200 OK`() {
        val userId = 1L
        val date = "2025-06-16"
        val tasks = listOf(
            TaskResponseDto(
                id = 1L,
                category = "운동",
                content = "산책하기",
                dueDate = LocalDateTime.of(2025, 6, 16, 9, 0),
                status = TaskStatus.COMPLETE,
                taskImage = null,
                scope = TaskScope.PUBLIC,
                nickname = "user1"
            ),
            TaskResponseDto(
                id = 2L,
                category = "식사",
                content = "점심 챙기기",
                dueDate = LocalDateTime.of(2025, 6, 16, 12, 0),
                status = TaskStatus.INCOMPLETE,
                taskImage = "image.jpg",
                scope = TaskScope.PRIVATE,
                nickname = "user1"
            )
        )

        doReturn(tasks).`when`(userService).getTasksByUser(userId, LocalDate.parse(date))

        mockMvc.perform(get("/users/{userId}/tasks", userId).param("date", date))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.size()").value(2))
            .andExpect(jsonPath("$.data[0].category").value("운동"))
            .andExpect(jsonPath("$.data[0].status").value("COMPLETE"))
            .andExpect(jsonPath("$.data[1].taskImage").value("image.jpg"))
    }

    @Test
    fun `GET - 잘못된 날짜 포맷이면 500 INTERNAL_SERVER_ERROR`() {
        val userId = 1L
        val invalidDate = "16-06-2025" // yyyy-MM-dd 아님

        mockMvc.perform(get("/users/{userId}/tasks", userId).param("date", invalidDate))
            .andExpect(status().isInternalServerError)
    }

    @Test
    fun `POST - 로그아웃 시 200 OK 및 쿠키 제거`() {
        doNothing().`when`(userService).logout()

        mockMvc.perform(post("/users/logout"))
            .andExpect(status().isOk)
            .andExpect(header().stringValues("Set-Cookie", org.hamcrest.Matchers.hasItems(
                org.hamcrest.Matchers.containsString("accessToken=;"),
                org.hamcrest.Matchers.containsString("refreshToken=;")
            )))
    }

    @Test
    fun `POST - 로그아웃 시 Service에서 예외 발생 시 500 INTERNAL_SERVER_ERROR`() {
        doThrow(RuntimeException("로그아웃 실패"))
            .`when`(userService).logout()

        mockMvc.perform(post("/users/logout"))
            .andExpect(status().isInternalServerError)
    }

    @Test
    fun `POST - 유효한 refreshToken으로 accessToken 재발급 시 200 OK`() {
        val refreshToken = "valid-refresh-token"
        val newAccessToken = "new-access-token"

        doReturn(newAccessToken).`when`(userService).reissueAccessToken(refreshToken)

        mockMvc.perform(
            post("/users/reissue")
                .cookie(Cookie("refreshToken", refreshToken))
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("accessToken=")))
    }

    @Test
    fun `POST - 유효하지 않은 refreshToken이면 401 UNAUTHORIZED`() {
        val invalidRefreshToken = "invalid-token"

        doThrow(AuthTokenException(ErrorCode.UNAUTHORIZED))
            .`when`(userService).reissueAccessToken(invalidRefreshToken)

        mockMvc.perform(
            post("/users/reissue")
                .cookie(Cookie("refreshToken", invalidRefreshToken))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `GET - 로그인된 사용자는 자신의 ID를 반환`() {
        // given
        val userId = 42L

        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt") // getCurrentUserId 확장함수의 패키지명을 정확히 넣기
        every { getCurrentUserId() } returns userId

        mockMvc.perform(get("/users/me"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(userId))
    }

    @Test
    fun `GET - 비로그인 사용자는 401 UNAUTHORIZED`() {
        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt")
        every { getCurrentUserId() } throws AuthTokenException(ErrorCode.UNAUTHORIZED)

        mockMvc.perform(get("/users/me"))
            .andExpect(status().isUnauthorized)
    }




}