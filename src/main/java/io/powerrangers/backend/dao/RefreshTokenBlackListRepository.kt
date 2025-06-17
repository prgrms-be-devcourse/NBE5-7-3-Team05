package io.powerrangers.backend.dao

import io.powerrangers.backend.entity.RefreshTokenBlackList
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import java.time.LocalDateTime

interface RefreshTokenBlackListRepository : JpaRepository<RefreshTokenBlackList, Long> {
    fun existsByRefreshToken_RefreshToken(refreshToken: String): Boolean
    @Modifying
    fun deleteByCreatedAtBefore(dateTime: LocalDateTime): Long
}
