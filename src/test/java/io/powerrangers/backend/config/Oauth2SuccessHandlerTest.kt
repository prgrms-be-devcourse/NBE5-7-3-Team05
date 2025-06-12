package io.powerrangers.backend.config

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.powerrangers.backend.dao.UserRepository
import io.powerrangers.backend.dto.Role
import io.powerrangers.backend.dto.TokenPair
import io.powerrangers.backend.dto.UserDetails
import io.powerrangers.backend.entity.RefreshToken
import io.powerrangers.backend.entity.User
import io.powerrangers.backend.exception.AuthTokenException
import io.powerrangers.backend.exception.ErrorCode
import io.powerrangers.backend.service.JwtProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.Authentication

class Oauth2SuccessHandlerTest {

    val userRepository = mockk<UserRepository>()
    val jwtProvider = mockk<JwtProvider>()
    val request = mockk<HttpServletRequest>(relaxed = true)
    val response = mockk<HttpServletResponse>(relaxed = true)
    val authentication = mockk<Authentication>()

    val oauth2SuccessHandler = Oauth2SuccessHandler(userRepository, jwtProvider, "baseUrl")

    @Test
    fun `유효한 refresh token이 없다면 새로 발급하여 리다이렉트 한다`() {
        // given
        val userId = -1L
        val accessToken = "access-token"
        val refreshTokenStr = "refresh-token"

        val userDetails = mockk<UserDetails> {
            every { id } returns userId
        }

        val user = User(
            id = userId,
            role = Role.USER,
            nickname = "test",
            email = "test@email.com",
            provider = "kakao",
            providerId = "abc123",
            profileImage = "profile"
        )

        val tokenPair = TokenPair(
            accessToken = accessToken,
            refreshToken = refreshTokenStr
        )

        every { authentication.principal } returns userDetails
        every { userRepository.findByIdOrNull(userId) } returns user
        every { jwtProvider.findValidRefreshToken(userId) } returns null
        every { jwtProvider.generateTokenPair(user) } returns tokenPair

        // when
        oauth2SuccessHandler.onAuthenticationSuccess(request, response, authentication)

        // then
        verify { response.addHeader(any(), any()) }
    }

    @Test
    fun `유효한 refresh token이 있으면 그것을 사용하여 리다이렉트 한다`() {
        // given
        val userId = -1L
        val accessToken = "access-token"
        val refreshTokenStr = "refresh-token"

        val userDetails = mockk<UserDetails> {
            every { id } returns userId
        }

        val user = User(
            id = userId,
            role = Role.USER,
            nickname = "test",
            email = "test@email.com",
            provider = "kakao",
            providerId = "abc123",
            profileImage = "profile"
        )

        val refreshToken = mockk<RefreshToken> {
            every { refreshToken } returns refreshTokenStr
        }

        every { authentication.principal } returns userDetails
        every { userRepository.findByIdOrNull(userId) } returns user
        every { jwtProvider.findValidRefreshToken(userId) } returns refreshToken
        every { jwtProvider.issueAccessToken(userId, user.role) } returns accessToken

        // when
        oauth2SuccessHandler.onAuthenticationSuccess(request, response, authentication)

        // then
        verify { response.addHeader(any(), any()) }
    }

    @Test
    fun `존재하지 않는 유저일 경우 AuthTokenException 발생`() {
        // given
        val userDetails = mockk<UserDetails> {
            every { id } returns -1L
        }

        // when
        every { authentication.principal } returns userDetails
        every { userRepository.findByIdOrNull(-1L) } returns null

        // then
        val exception = shouldThrow<AuthTokenException> {
            oauth2SuccessHandler.onAuthenticationSuccess(request, response, authentication)
        }
        exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND
    }

    @Test
    fun `authentication의 principal이 UserDetails가 아니면 ClassCastException`() {
        // when
        every { authentication.principal } returns "ClassCastException 발생!"

        // then
        shouldThrow<ClassCastException> {
            oauth2SuccessHandler.onAuthenticationSuccess(request, response, authentication)
        }
    }
}
