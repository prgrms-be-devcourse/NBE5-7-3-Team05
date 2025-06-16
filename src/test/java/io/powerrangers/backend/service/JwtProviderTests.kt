package io.powerrangers.backend.service

import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.powerrangers.backend.dao.TokenRepository
import io.powerrangers.backend.dto.Role
import io.powerrangers.backend.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JwtProviderTests {
    val tokenRepository = mockk<TokenRepository>()
    val jwtProvider = JwtProvider(
        tokenRepository = tokenRepository,
        accessTokenExpiration = 3600,
        refreshTokenExpiration = 7200,
        jwtSecret = "test-secret".repeat(10)
    )

    @Test
    fun `accessToken으로부터 userId와 role을 추출할 수 있어야 한다`() {
        val token = jwtProvider.issueAccessToken(1L, Role.USER)
        val parsed = jwtProvider.parseToken(token)

        parsed.userId shouldBe 1L
        parsed.role shouldBe Role.USER.name
    }

    @Test
    fun `user에게 새로운 accessToken과 refreshToken을 발급할 수 있다`() {
        val user = User(
            id = 1L,
            nickname = "taeho",
            profileImage = null,
            provider = "kakao",
            providerId = "12345",
            email = "taeho@test.com",
            intro = "hi",
            role = Role.USER
        )

        every {
            tokenRepository.save(user, any())
        } just Runs

        val tokenPair = jwtProvider.generateTokenPair(user)

        assertThat(tokenPair.accessToken).isNotNull
        assertThat(tokenPair.refreshToken).isNotNull
    }

    @Test
    fun `유효한 토큰이면 true를 반환한다`() {
        val token = jwtProvider.issueAccessToken(1L, Role.USER)
        val isValid = jwtProvider.validateToken(token)
        isValid shouldBe true
    }

    @Test
    fun `잘못된 토큰이면 false를 반환한다`() {
        val isValid = jwtProvider.validateToken("abc.def.ghi")
        isValid shouldBe false
    }

    @Test
    fun `빈 토큰이면 false를 반환한다`() {
        val isValid = jwtProvider.validateToken("")
        isValid shouldBe false
    }
}