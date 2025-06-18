package io.powerrangers.backend.config

import io.powerrangers.backend.dto.Role
import io.powerrangers.backend.dto.TokenBody
import io.powerrangers.backend.dto.UserDetails
import io.powerrangers.backend.exception.AuthTokenException
import io.powerrangers.backend.exception.CustomOAuth2AuthenticationFailureHandler
import io.powerrangers.backend.exception.ErrorCode
import io.powerrangers.backend.service.JwtProvider
import io.powerrangers.backend.service.UserService
import io.powerrangers.backend.utils.ACCESS_TOKEN
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@ActiveProfiles("test")
@WebMvcTest(controllers = [AuthTestController::class])
@Import(SecurityConfig::class, JwtAuthenticationFilter::class)
internal class JwtAuthenticationFilterTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var jwtProvider: JwtProvider

    @MockitoBean
    lateinit var userService: UserService

    @MockitoBean
    lateinit var oauth2SuccessHandler: Oauth2SuccessHandler

    @MockitoBean
    lateinit var failureHandler: CustomOAuth2AuthenticationFailureHandler

    @Test
    fun `필터 내 토큰 검증 성공`() {
        val token = "valid token"
        val userDetails = UserDetails(
            nickname = "name",
            email = "email",
            providerId = "kakao",
            profileImage = "profile",
        )
        userDetails.role = Role.USER

        val tokenBody = TokenBody(
            userId = -1L,
            role = Role.USER.name
        )
        val cookie = Cookie(ACCESS_TOKEN, token)

        `when`(jwtProvider.validateToken(token)).thenReturn(true)
        `when`(jwtProvider.parseToken(token)).thenReturn(tokenBody)
        `when`(userService.getUserDetails(tokenBody.userId)).thenReturn(userDetails)

        mockMvc.get("/security-endpoint") { // 인증이 필요한 경로
            cookie(cookie)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `잘못된 토큰으로 필터에서 AuthTokenException 발생 검증`() {
        val token = "invalid token"
        val cookie = Cookie(ACCESS_TOKEN, token)

        `when`(jwtProvider.validateToken(token)).thenReturn(false)

        mockMvc.get("/security-endpoint") { // 인증이 필요한 경로
            cookie(cookie)
        }
            .andExpect {
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.errorCode") { value(HttpStatus.UNAUTHORIZED.toString()) }
            }
            .andDo { print() }
    }

    @Test
    fun `토큰 내에 들어있는 id를 가진 유저가 없어 AuthTokenException 발생`() {
        val token = "invalid token"
        val tokenBody = TokenBody(
            userId = -1L,
            role = Role.USER.name
        )
        val cookie = Cookie(ACCESS_TOKEN, token)

        `when`(jwtProvider.validateToken(token)).thenReturn(true)
        `when`(jwtProvider.parseToken(token)).thenReturn(tokenBody)
        `when`(userService.getUserDetails(-1L)).thenThrow(AuthTokenException(ErrorCode.USER_NOT_FOUND))

        mockMvc.get("/security-endpoint") { // 인증이 필요한 경로
            cookie(cookie)
        }
            .andExpect {
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.errorCode") { value(HttpStatus.NOT_FOUND.toString()) }
            }
            .andDo { print() }
    }

    @Test
    fun `토큰이 없어 필터에서 AuthTokenException 발생`() {
        mockMvc.get("/security-endpoint") // 인증 필요한 경로
            .andExpect {
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.errorCode") { value(HttpStatus.UNAUTHORIZED.toString()) }
            }
            .andDo { print() }
    }
}