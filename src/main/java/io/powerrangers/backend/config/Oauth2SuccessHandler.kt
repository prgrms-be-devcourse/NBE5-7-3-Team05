package io.powerrangers.backend.config

import io.powerrangers.backend.dao.UserRepository
import io.powerrangers.backend.dto.UserDetails
import io.powerrangers.backend.exception.AuthTokenException
import io.powerrangers.backend.exception.ErrorCode
import io.powerrangers.backend.service.JwtProvider
import io.powerrangers.backend.utils.createAccessCookie
import io.powerrangers.backend.utils.createRefreshCookie
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class Oauth2SuccessHandler(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    @Value("\${custom.oauth2.redirect-url}")
    private val baseUrl :String
) : SimpleUrlAuthenticationSuccessHandler() {

    @Throws(java.io.IOException::class, ServletException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val principal = authentication.principal as UserDetails
        val findUser = userRepository.findByIdOrNull(principal.id) ?: throw AuthTokenException(ErrorCode.USER_NOT_FOUND)

        var accessToken: String
        var refreshToken: String

        val validRefreshToken = jwtProvider.findValidRefreshToken(principal.id!!)
        // DB에 유효한 refresh token이 있다면 그것을 사용하고, 아니라면 모두 새로 만들어야 한다.
        validRefreshToken?.let {
            accessToken = jwtProvider.issueAccessToken(findUser.id!!, findUser.role)
            refreshToken = it.refreshToken
        } ?: run {
            val tokenPair = jwtProvider.generateTokenPair(findUser)
            accessToken = tokenPair.accessToken
            refreshToken = tokenPair.refreshToken
        }

        // 쿠키 생성 및 추가
        val accessCookie: ResponseCookie = createAccessCookie(accessToken)
        val refreshCookie: ResponseCookie = createRefreshCookie(refreshToken)

        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, accessCookie.toString())
        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, refreshCookie.toString())

        val url = UriComponentsBuilder.fromUriString(baseUrl)
            .queryParam("userId", findUser.id)
            .build()
            .toString()

        redirectStrategy.sendRedirect(request, response, url)
    }
}
