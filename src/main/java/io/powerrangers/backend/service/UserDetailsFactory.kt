package io.powerrangers.backend.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.powerrangers.backend.dto.UserDetails
import io.powerrangers.backend.exception.AuthTokenException
import io.powerrangers.backend.exception.ErrorCode
import lombok.extern.slf4j.Slf4j
import org.springframework.security.oauth2.core.user.OAuth2User
import java.util.*

private val log = KotlinLogging.logger {}

@Suppress("UNCHECKED_CAST")

fun userDetails(oAuth2User: OAuth2User, providerId: String): UserDetails {
    val attributes = oAuth2User.attributes
    when (providerId.lowercase(Locale.getDefault())) {
        "google" -> {
            return UserDetails(
                name = attributes["name"].toString(),
                email = attributes["email"].toString(),
                providerId = attributes["sub"].toString(),
                profileImage = attributes["picture"].toString(),
                attributes = attributes
            )
        }

        "kakao" -> {
            val properties = attributes["properties"] as MutableMap<String?, String?>
            return UserDetails(
                name = properties["nickname"].toString(),
                email = attributes["id"].toString() + "@kakao.com",
                providerId = attributes["id"].toString(),
                profileImage = attributes["profile_image"].toString(),
                attributes = attributes
            )
        }

        "naver" -> {
            val response = attributes["response"] as MutableMap<String?, String?>
            return UserDetails(
                name = response["name"].toString(),
                email = response["email"].toString(),
                providerId = response["id"].toString(),
                profileImage = response["profile_image"].toString(),
                attributes = attributes
            )
        }

        else -> {
            log.warn { "[인증 실패] 지원하지 않는 providerId: $providerId"}
            throw AuthTokenException(ErrorCode.UNAUTHORIZED)
        }
    }
}

