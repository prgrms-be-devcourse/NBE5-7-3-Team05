package io.powerrangers.backend.dao

import io.powerrangers.backend.entity.RefreshTokenBlackList
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenBlackListRepository : JpaRepository<RefreshTokenBlackList, Long> {
    fun existsByRefreshToken_RefreshToken(refreshToken: String): Boolean
}
