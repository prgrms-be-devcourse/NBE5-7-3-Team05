package io.powerrangers.backend.service

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.powerrangers.backend.dao.UserRepository
import io.powerrangers.backend.dto.Role
import io.powerrangers.backend.dto.UserDetails
import io.powerrangers.backend.entity.User
import io.powerrangers.backend.utils.genUserDetails
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import kotlin.test.Test

class CustomOauth2UserServiceTests {
    private val userRepository = mockk<UserRepository>()
    val mockDelegate = mockk<DefaultOAuth2UserService>()

    val service = CustomOauth2UserService(userRepository, mockDelegate)

    @Test
    fun `loadUser를 하면 적절한 UserDetails을 반환해야 합니다`() {
        // given
        mockkStatic("io.powerrangers.backend.utils.UserDetailsFactoryKt")
        val userRequest = mockk<OAuth2UserRequest>()
        val fakeOAuth2User: OAuth2User = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority(Role.USER.name)),
            mapOf(
                "email" to "test@email.com",
                "nickname" to "tester",
            ),
            "email" // attribute key used for name
        )

        val dummyUserDetails = UserDetails(
            email = "test@email.com",
            nickname = "tester",
            providerId = "12345",
            role = Role.USER
        )

        val user = User(
            id = 1L,
            nickname = "tester",
            provider = "kakao",
            providerId = "12345",
            email = "test@email.com",
            intro = "hi",
            role = Role.USER
        )
        every { mockDelegate.loadUser(userRequest) } returns fakeOAuth2User

        every { userRequest.clientRegistration.registrationId } returns "kakao"

        every { genUserDetails(fakeOAuth2User, "kakao") } returns dummyUserDetails

        every { userRepository.findByEmail("test@email.com") } returns null

        every { userRepository.existsByNickname("tester") } returns false

        every { userRepository.save(any()) } returns user

        // when
        val result = service.loadUser(userRequest)

        // then
        every { userRepository.save(any()) } returns User(
            id = 1L,
            nickname = "tester",
            email = "test@email.com",
            provider = "kakao",
            providerId = "12345",
            profileImage = "profile.jpg",
            role = Role.USER
        )

        result.name shouldBe "tester"
        result.authorities.map { it.authority } shouldContain Role.USER.name
    }
}