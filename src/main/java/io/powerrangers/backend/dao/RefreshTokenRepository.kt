package io.powerrangers.backend.dao

import io.powerrangers.backend.entity.RefreshToken
import io.powerrangers.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findUserByRefreshToken(refreshTokenValue: String?): User?
}
