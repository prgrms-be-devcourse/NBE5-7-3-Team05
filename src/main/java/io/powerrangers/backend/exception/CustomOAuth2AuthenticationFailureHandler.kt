package io.powerrangers.backend.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component

@Component
class CustomOAuth2AuthenticationFailureHandler : AuthenticationFailureHandler {
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val errorMessage = if (exception is OAuth2AuthenticationException) {
            "로그인에 실패했습니다. ${exception.error.description}"
        } else {
            "로그인에 실패했습니다."
        }

        request.session.setAttribute("error", errorMessage)
        response.sendRedirect("/oauth2/login")
    }
}
