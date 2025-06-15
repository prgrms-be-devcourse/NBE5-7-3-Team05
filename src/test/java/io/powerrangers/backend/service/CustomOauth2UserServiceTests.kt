package io.powerrangers.backend.service

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.powerrangers.backend.dao.UserRepository
import io.powerrangers.backend.dto.Role
import io.powerrangers.backend.dto.UserDetails
import io.powerrangers.backend.entity.User
import io.powerrangers.backend.utils.genUserDetails
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import kotlin.test.Test

class CustomOauth2UserServiceTests {
    private val userRepository = mockk<UserRepository>()
    val mockDelegate = mockk<DefaultOAuth2UserService>()

    val service = CustomOauth2UserService(userRepository, mockDelegate)

    @Test
    fun `처음 가입하는 유저는 loadUser() 호출 시 새로운 유저를 만들고 UserDetails을 반환해야 합니다`() {
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

        result.name shouldBe "tester"
        result.authorities.map { it.authority } shouldContain Role.USER.name
    }

    @Test
    fun `이미 가입한 유저는 loadUser() 호출 시 DB에서 유저 정보를 가져와 UserDetails를 반환한다`() {

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

        every { userRepository.findByEmail("test@email.com") } returns user

        // when

        val result = service.loadUser(userRequest)

        verify(exactly = 0) {
            userRepository.save(any())
        }

        result.name shouldBe "tester"
        result.authorities.map { it.authority } shouldContain Role.USER.name
    }

    @Test
    fun `로그인하는 유저가 이미 같은 이메일로 가입한 적이 있다면 예외를 반환한다`() {
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

        every { userRequest.clientRegistration.registrationId } returns "google"

        every { genUserDetails(fakeOAuth2User, "kakao") } returns dummyUserDetails

        every { userRepository.findByEmail("test@email.com") } returns user

        // when, then
        assertThrows<OAuth2AuthenticationException> {
            service.loadUser(userRequest)
        }

    }
}