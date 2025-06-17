package io.powerrangers.backend.dao

import io.powerrangers.backend.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import java.time.LocalDateTime

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    @Modifying
    fun deleteByCreatedAtBefore(dateTime: LocalDateTime): Long
}
