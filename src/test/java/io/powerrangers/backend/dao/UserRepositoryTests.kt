package io.powerrangers.backend.dao

import io.github.oshai.kotlinlogging.KotlinLogging
import io.powerrangers.backend.dto.Role
import io.powerrangers.backend.entity.User
import io.powerrangers.backend.util.genUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private val log = KotlinLogging.logger {}

@DataJpaTest
class UserRepositoryTests(
    @Autowired val userRepository: UserRepository
) {

    @Test
    fun `UserRepository 주입 테스트`() {

        log.info { userRepository }
        assertThat(userRepository).isNotNull

    }

    @Test
    fun `UserRepository 정상 저장 테스트`() {

        // given
        val targetNickname = "test1"
        val targetProfileImage = "test1"
        val targetProvider = "test1"
        val targetProviderId = "test1"
        val targetEmail = "test1"
        val targetIntro = "test1"
        val targetRole = Role.USER

        val user = genUser(
            targetNickname,
            targetProfileImage,
            targetProvider,
            targetProviderId,
            targetEmail,
            targetIntro,
            targetRole
        )

        val saved: User = userRepository.save(user)

        assertNotNull(saved)
        assertEquals(saved.nickname, targetNickname)
        assertEquals(saved.profileImage, targetProfileImage)
        assertEquals(saved.profileImage, targetProviderId)
        assertEquals(saved.email, targetEmail)
        assertEquals(saved.intro, targetIntro)
        assertEquals(saved.role, targetRole)
    }

    @Test
    fun `findByIdOrNull 정상적인 Id 값을 넣었을 때 성공 테스트`() {
        // given

        val targetNickname = "test1"
        val targetProfileImage = "test1"
        val targetProvider = "test1"
        val targetProviderId = "test1"
        val targetEmail = "test1"
        val targetIntro = "test1"
        val targetRole = Role.USER

        val user = genUser(
            targetNickname,
            targetProfileImage,
            targetProvider,
            targetProviderId,
            targetEmail,
            targetIntro,
            targetRole
        )

        val targetUser = userRepository.save(user)

        // when

        val actualUser = userRepository.findByIdOrNull(targetUser.id)


        // then

        assertNotNull(actualUser)
        assertEquals(actualUser.id, user.id)
        assertEquals(actualUser.nickname, user.nickname)
        assertEquals(actualUser.profileImage, user.profileImage)
        assertEquals(actualUser.email, user.email)
        assertEquals(actualUser.intro, user.intro)
        assertEquals(actualUser.role, user.role)

    }

    @Test
    fun `findByIdOrNull 정상적이지 않은 Id 값을 넣었을 때 실패 테스트`() {

        val targetNickname = "test1"
        val targetProfileImage = "test1"
        val targetProvider = "test1"
        val targetProviderId = "test1"
        val targetEmail = "test1"
        val targetIntro = "test1"
        val targetRole = Role.USER

        val user = genUser(
            targetNickname,
            targetProfileImage,
            targetProvider,
            targetProviderId,
            targetEmail,
            targetIntro,
            targetRole
        )

        val targetUser = userRepository.save(user)

        // when

        val actualUser = userRepository.findByIdOrNull(999L)

        // then

        assertNull(actualUser)
    }
    
    @Test
    fun `findByNickname 정상적인 닉네임을 넣었을 때 성공 테스트`() {

        // given
        val user1 = genUser("test", "img", "google", "pid1", "email1", "intro")
        val user2 = genUser("testA", "img", "google", "pid2", "email2", "intro")
        val user3 = genUser("testB", "img", "google", "pid3", "email3", "intro")
        val user4 = genUser("other", "img", "google", "pid4", "email4", "intro")
        val user5 = genUser("Nottest", "img", "google", "pid5", "email5", "intro")
        val user6 = genUser("TEST", "img", "google", "pid6", "email6", "intro")
        val saveAll = userRepository.saveAll(listOf(user1, user2, user3, user4, user5, user6))

        // when
        val actualUsers = userRepository.findByNickname("test")

        // then

        assertNotNull(actualUsers)
        assertEquals(actualUsers.size, 3)
        assertTrue(actualUsers.all { it.nickname.startsWith("test") } )

    }
    @Test
    fun `findByNickname 없는 prefix를 넣었을 때 실패 테스트`() {

        // given
        val user1 = genUser("test", "img", "google", "pid1", "email1", "intro")
        val user2 = genUser("testA", "img", "google", "pid2", "email2", "intro")
        val user3 = genUser("testB", "img", "google", "pid3", "email3", "intro")
        val user4 = genUser("other", "img", "google", "pid4", "email4", "intro")
        val user5 = genUser("Nottest", "img", "google", "pid5", "email5", "intro")
        val user6 = genUser("TEST", "img", "google", "pid6", "email6", "intro")
        val saveAll = userRepository.saveAll(listOf(user1, user2, user3, user4, user5, user6))

        // when
        val actualUsers = userRepository.findByNickname("NonPrefixTeste")

        // then

        assertNotNull(actualUsers)
        assertEquals(actualUsers.size, 0)
        assertTrue(actualUsers.isEmpty())

    }

    @Test
    fun `findByNickname 중간에 들어가는 prefix를 넣었을 때 테스트`() {

        // given
        val user1 = genUser("test", "img", "google", "pid1", "email1", "intro")
        val user2 = genUser("testA", "img", "google", "pid2", "email2", "intro")
        val user3 = genUser("testB", "img", "google", "pid3", "email3", "intro")
        val user4 = genUser("other", "img", "google", "pid4", "email4", "intro")
        val user5 = genUser("Nottest", "img", "google", "pid5", "email5", "intro")
        val user6 = genUser("TEST", "img", "google", "pid6", "email6", "intro")
        val saveAll = userRepository.saveAll(listOf(user1, user2, user3, user4, user5, user6))

        // when
        val actualUsers = userRepository.findByNickname("est")

        // then

        assertNotNull(actualUsers)
        assertEquals(actualUsers.size, 0)
        assertTrue(actualUsers.isEmpty())

    }

    @Test
    fun `existsByNickname DB에 있는 닉네임 조회시 성공 테스트`() {

        // given
        val user1 = genUser("test", "img", "google", "pid1", "email1", "intro")

        userRepository.save(user1)

        // when
        val actualUser = userRepository.existsByNickname("test")

        // then
        assertTrue(actualUser)

    }

    @Test
    fun `existsByNickname DB에 없는 닉네임 조회시 실패 테스트`() {

        // given
        val user1 = genUser("test", "img", "google", "pid1", "email1", "intro")

        userRepository.save(user1)

        // when
        val actualUser = userRepository.existsByNickname("NoneExistNickname")

        // then
        assertFalse(actualUser)

    }

    @Test
    fun `existsByNickname DB에 공백을 조회했을 때 테스트`() {

        // given
        val user1 = genUser("test", "img", "google", "pid1", "email1", "intro")

        userRepository.save(user1)

        // when
        val actualUser = userRepository.existsByNickname("")

        // then
        assertFalse(actualUser)

    }
}

