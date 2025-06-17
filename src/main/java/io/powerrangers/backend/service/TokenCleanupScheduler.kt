package io.powerrangers.backend.service

import io.powerrangers.backend.dao.TokenRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime

@Service
class TokenCleanupScheduler(
    private val tokenRepository: TokenRepository,
    @Value("\${custom.jwt.validation.refresh}") private val expiredTime: Long
) {
    @Scheduled(cron = "0 0 3 * * *")
    fun cleanUpOldToken() {
        val threshold = LocalDateTime.now().minus(Duration.ofMillis(expiredTime))
        tokenRepository.cleanUpOldToken(threshold)
    }
}