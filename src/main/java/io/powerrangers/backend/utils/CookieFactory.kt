package io.powerrangers.backend.utils

import org.springframework.http.ResponseCookie
import java.time.Duration

private val ACCESS_TOKEN_EXPIRATION: Duration = Duration.ofHours(2)
private val REFRESH_TOKEN_EXPIRATION: Duration = Duration.ofDays(14)
const val ACCESS_TOKEN: String = "accessToken"
const val REFRESH_TOKEN: String = "refreshToken"

fun createAccessCookie(value: String): ResponseCookie {
    return ResponseCookie.from(ACCESS_TOKEN, value)
        .httpOnly(true) // 프론트에서 js로 쿠키 접근 x
        .path("/") // 모든 경로에서 쿠키 사용 가능
        .sameSite("Lax") // 같은 도메인에서 get/post 는 쿠키 전송이 가능함
        .maxAge(ACCESS_TOKEN_EXPIRATION) // 쿠키 유효시간
        .build()
}

fun createRefreshCookie(value: String): ResponseCookie {
    return ResponseCookie.from(REFRESH_TOKEN, value)
        .httpOnly(true)
        .path("/")
        .sameSite("Lax")
        .maxAge(REFRESH_TOKEN_EXPIRATION)
        .build()
}

fun deleteAccessCookie(): ResponseCookie {
    return ResponseCookie.from(ACCESS_TOKEN, "")
        .httpOnly(true)
        .path("/")
        .sameSite("Lax")
        .maxAge(Duration.ZERO)
        .build()
}

fun deleteRefreshCookie(): ResponseCookie {
    return ResponseCookie.from(REFRESH_TOKEN, "")
        .httpOnly(true)
        .path("/")
        .sameSite("Lax")
        .maxAge(Duration.ZERO)
        .build()
}

