package io.powerrangers.backend.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import io.powerrangers.backend.dto.UserDetails
import io.powerrangers.backend.exception.AuthTokenException
import io.powerrangers.backend.exception.ErrorCode
import org.springframework.security.oauth2.core.user.OAuth2User
import java.util.*

private val log = KotlinLogging.logger {}

@Suppress("UNCHECKED_CAST")
fun genUserDetails(oAuth2User: OAuth2User, providerId: String): UserDetails {
    val attributes = oAuth2User.attributes
    when (providerId.lowercase(Locale.getDefault())) {
        "google" -> {
            return UserDetails(
                nickname = attributes["name"].toString(),
                email = attributes["email"].toString(),
                providerId = attributes["sub"].toString(),
                profileImage = attributes["picture"].toString(),
                oauthAttributes = attributes
            )
        }

        "kakao" -> {
            val properties = attributes["properties"] as MutableMap<String?, String?>
            return UserDetails(
                nickname = properties["nickname"].toString(),
                email = attributes["id"].toString() + "@kakao.com",
                providerId = attributes["id"].toString(),
                profileImage = attributes["profile_image"].toString(),
                oauthAttributes = attributes
            )
        }

        "naver" -> {
            val response = attributes["response"] as MutableMap<String?, String?>
            return UserDetails(
                nickname = response["name"].toString(),
                email = response["email"].toString(),
                providerId = response["id"].toString(),
                profileImage = response["profile_image"].toString(),
                oauthAttributes = attributes
            )
        }

        else -> {
            log.warn { "[인증 실패] 지원하지 않는 providerId: $providerId"}
            throw AuthTokenException(ErrorCode.UNAUTHORIZED)
        }
    }
}

