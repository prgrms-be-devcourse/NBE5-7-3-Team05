package io.powerrangers.backend.dao.adapter

import io.powerrangers.backend.dao.RefreshTokenBlackListRepository
import io.powerrangers.backend.dao.RefreshTokenRepository
import io.powerrangers.backend.dao.TokenRepository
import io.powerrangers.backend.entity.RefreshToken
import io.powerrangers.backend.entity.RefreshTokenBlackList
import io.powerrangers.backend.entity.User
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
class RefreshTokenRepositoryAdapter (
    private val refreshTokenRepository: RefreshTokenRepository,
    private val refreshTokenBlackListRepository: RefreshTokenBlackListRepository,
    private val entityManager: EntityManager
) : TokenRepository {

    @Transactional
    override fun save(user: User, refreshToken: String) {
        val token = RefreshToken(
            user = user,
            refreshToken = refreshToken
        )
        refreshTokenRepository.save(token)
    }

    @Transactional(readOnly = true)
    override fun tokenBlackList(refreshToken: String): Boolean {
        return refreshTokenBlackListRepository.existsByRefreshToken_RefreshToken(refreshToken)
    }

    @Transactional
    override fun addBlackList(refreshToken: RefreshToken): RefreshTokenBlackList {
        val blackList = RefreshTokenBlackList(
            refreshToken = refreshToken
        )
        return refreshTokenBlackListRepository.save(blackList)
    }

    @Transactional(readOnly = true)
    override fun findValidRefreshToken(userId: Long): RefreshToken? {
        val jpql =
            """
                SELECT rt 
                FROM RefreshToken rt 
                LEFT JOIN RefreshTokenBlackList rtbl 
                ON rt.user.id = :userId AND rtbl.refreshToken IS NULL
            """.trimIndent()
        return entityManager.createQuery(jpql, RefreshToken::class.java)
            .setParameter("userId", userId)
            .resultStream
            .findFirst()
            .orElse(null)
    }

    @Transactional(readOnly = true)
    override fun findAllRefreshTokensByUserId(userId: Long): List<RefreshToken> {
        val jpql = "SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId"
        return entityManager.createQuery(jpql, RefreshToken::class.java)
            .setParameter("userId", userId)
            .resultList
    }

    @Transactional
    override fun cleanUpOldToken(dateTime: LocalDateTime) {
        refreshTokenBlackListRepository.deleteByCreatedAtBefore(dateTime)
        refreshTokenRepository.deleteByCreatedAtBefore(dateTime)
    }
}
