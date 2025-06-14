package io.powerrangers.backend.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.powerrangers.backend.dao.UserRepository
import io.powerrangers.backend.entity.User
import io.powerrangers.backend.utils.genUserDetails
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.util.*

private val log = KotlinLogging.logger {}

@Service
class CustomOauth2UserService(
    private val userRepository: UserRepository,
    private val delegate: DefaultOAuth2UserService
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = delegate.loadUser(userRequest)

        val provider = userRequest.clientRegistration.registrationId // google, naver, kakao
        val userDetails = genUserDetails(oAuth2User, provider)
        var nickname = userDetails.nickname

        val findUser = userRepository.findByEmail(userDetails.email)
            ?: run {
                while (userRepository.existsByNickname(nickname)) {
                    nickname = generateTempNickname(nickname)
                }

                val newUser = User(
                    nickname = nickname,
                    email = userDetails.email,
                    provider = provider,
                    providerId = userDetails.providerId,
                    profileImage = userDetails.profileImage
                )

                userRepository.save(newUser)
            }

        if (findUser.provider != provider) {
            throw OAuth2AuthenticationException(
                OAuth2Error("already_signed_in", "이미 다른 제공자로 가입한 적이 있습니다.", null)
            )
        }

        userDetails.id = findUser.id
        userDetails.role = findUser.role
        return userDetails
    }

    private fun generateTempNickname(nickname: String): String {
        val adjectives = arrayOf("귀여운 ", "멋진 ", "행복한 ", "용감한 ")
        val powerRangers = arrayOf("레드 ", "블루 ", "옐로우 ", "그린 ", "핑크 ")

        val adj = adjectives[Random().nextInt(adjectives.size)]
        val noun = powerRangers[Random().nextInt(powerRangers.size)]
        val number = Random().nextInt(1000)

        return "$adj $noun $nickname $number" // 귀여운 레드 홍길동123
    }
}
