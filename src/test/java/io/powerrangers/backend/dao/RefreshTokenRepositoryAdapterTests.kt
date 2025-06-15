package io.powerrangers.backend.dao

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.powerrangers.backend.dao.adapter.RefreshTokenRepositoryAdapter
import io.powerrangers.backend.dto.Role
import io.powerrangers.backend.entity.RefreshToken
import io.powerrangers.backend.entity.User
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(RefreshTokenRepositoryAdapter::class)
class RefreshTokenRepositoryAdapterTests {
    @Autowired
    lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired
    lateinit var refreshTokenBlackListRepository: RefreshTokenBlackListRepository

    @PersistenceContext
    lateinit var em: EntityManager

    lateinit var repository: TokenRepository

    @BeforeEach
    fun setUp() {
        repository = RefreshTokenRepositoryAdapter(
            refreshTokenRepository,
            refreshTokenBlackListRepository,
            em
        )
    }

    @Test
    fun `repository 주입 확인`() {
        repository shouldNotBe null
    }

    @Test
    fun `refresh token을 저장하면 DB에 잘 저장되고 조회할 수 있다`() {
        val user = genUser()
        em.persist(user)
        em.flush()

        val refreshToken = "refreshToken"
        repository.save(user, refreshToken)
        em.flush()

        val found = repository.findValidRefreshToken(user.id!!)
        found shouldNotBe null
        found!!.refreshToken shouldBe refreshToken
    }

    @Test
    fun `블랙리스트가 된 refreshToken이 있다면 true를 반환한다`() {
        val user = genUser()
        em.persist(user)

        val refreshToken = RefreshToken(
            user = user,
            refreshToken = "bad-token"
        )

        refreshTokenRepository.save(refreshToken)
        em.flush()
        em.clear()

        repository.addBlackList(refreshToken)
        em.flush()
        em.clear()

        val result = repository.tokenBlackList("bad-token")

        result shouldBe true
    }

    @Test
    fun `userId를 가지고 해당 유저가 소유한 모든 refreshToken을 반환한다`() {
        val user = genUser()
        em.persist(user)

        val token1 = RefreshToken(user = user, refreshToken = "token-1")
        val token2 = RefreshToken(user = user, refreshToken = "token-2")
        em.persist(token1)
        em.persist(token2)

        em.flush()
        em.clear()

        val tokens: List<RefreshToken> = repository.findAllRefreshTokensByUserId(user.id!!)

        tokens shouldHaveSize 2
        tokens.map { it.refreshToken } shouldContainAll listOf("token-1", "token-2")
    }

    private fun genUser(): User {
        return User(
            nickname = "han",
            profileImage = null,
            provider = "kakao",
            providerId = "12345",
            email = "han@test.com",
            intro = "hi",
            role = Role.USER
        )
    }
}
