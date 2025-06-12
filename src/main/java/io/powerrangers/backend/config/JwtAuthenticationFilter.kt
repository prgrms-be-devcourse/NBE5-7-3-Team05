package io.powerrangers.backend.config

import io.github.oshai.kotlinlogging.KotlinLogging
import io.powerrangers.backend.dto.TokenBody
import io.powerrangers.backend.dto.UserDetails
import io.powerrangers.backend.exception.AuthTokenException
import io.powerrangers.backend.exception.ErrorCode
import io.powerrangers.backend.service.CustomOauth2UserService
import io.powerrangers.backend.service.JwtProvider
import io.powerrangers.backend.util.ACCESS_TOKEN
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

private val log = KotlinLogging.logger {}

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val customOauth2UserService: CustomOauth2UserService,
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)
        if (token == null || !jwtProvider.validateToken(token)) {
            log.error { "토큰 유효성 검사에 실패했습니다." }
            handleAuthTokenException(response, AuthTokenException(ErrorCode.UNAUTHORIZED))
            return
        }

        val tokenBody: TokenBody = jwtProvider.parseToken(token)
        val userDetails: UserDetails
        try {
            userDetails = customOauth2UserService.getUserDetails(tokenBody.userId)
        } catch (e: AuthTokenException) {
            log.error { "토큰의 주인 유저를 찾을 수 없습니다." }
            handleAuthTokenException(response, e)
            return
        }

        val authentication: Authentication = UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )
        SecurityContextHolder.getContext().authentication = authentication
        filterChain.doFilter(request, response)
    }

    @Throws(IOException::class)
    private fun handleAuthTokenException(response: HttpServletResponse, e: AuthTokenException) {
        var message = e.errorCode.message
        if (e.provider != null) {
            message += " : " + e.provider
        }
        response.apply {
            status = HttpServletResponse.SC_UNAUTHORIZED
            contentType = "application/json"
            characterEncoding = "UTF-8"
            writer.write(
                """
            {
                "errorCode": "${e.errorCode.status}",
                "message": "$message"
            }
            """.trimIndent()
            )
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val antPathMatcher = AntPathMatcher()
        val path = request.requestURI
        return WHITE_LIST.stream()
            .anyMatch { pattern: String -> antPathMatcher.match(pattern, path) }
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        return request.cookies
            ?.firstOrNull { it.name == ACCESS_TOKEN }
            ?.value
    }
}

private val WHITE_LIST = mutableListOf(
    "/",  // 루트 요청 (홈 화면)
    "/test/**",  // 테스트용 API
    "/favicon.ico",  // 즐겨찾기 아이콘
    "/index.html",  // 정적 index
    "/css/**",  // 정적 CSS
    "/default-ui.css",
    "/js/**",  // 정적 JS
    "/webjars/**",  // 의존성 리소스(js 라이브러리 등)
    "/error",  // 에러 페이지 (Spring 내부에서 요청)
    "/login",  // 로그인 페이지
    "/oauth2/**",  // OAuth2 관련 리디렉션 URL
    "/users/reissue",  // access token 재발급 요청
    "/search.html",  // 검색 페이지
    "/follow-list.html",  // 팔로우 목록 페이지
    "/.well-known/appspecific/com.chrome.devtools.json",  // 크롬에서 날라오는 백엔드용 요청..?
    "/loginPage",
    "/images/**",
    "/fonts/**"
)