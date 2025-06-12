package io.powerrangers.backend.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Jwts.SIG
import io.jsonwebtoken.security.Keys
import io.powerrangers.backend.dao.TokenRepository
import io.powerrangers.backend.dto.Role
import io.powerrangers.backend.dto.TokenBody
import io.powerrangers.backend.dto.TokenPair
import io.powerrangers.backend.entity.RefreshToken
import io.powerrangers.backend.entity.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

private val log = KotlinLogging.logger {}

@Service
class JwtProvider(
    private val tokenRepository: TokenRepository,
    @Value("\${custom.jwt.validation.access}")
    private val accessTokenExpiration: Long,
    @Value("\${custom.jwt.validation.refresh}")
    private val refreshTokenExpiration: Long,
    @Value("\${custom.jwt.secrets.app-key}")
    private val jwtSecret: String
) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    private val parser by lazy {
        Jwts.parser().verifyWith(secretKey).build()
    }

    fun issueAccessToken(id: Long, role: Role): String {
        return issueToken(id, role, accessTokenExpiration)
    }

    fun issueRefreshToken(id: Long, role: Role): String {
        return issueToken(id, role, refreshTokenExpiration)
    }

    fun generateTokenPair(user: User): TokenPair {
        val id = user.id!!
        val role = user.role
        val accessToken = issueAccessToken(id, role)
        val refreshToken = issueRefreshToken(id, role)

        tokenRepository.save(user, refreshToken)

        return TokenPair(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun findValidRefreshToken(userId: Long): RefreshToken? {
        return tokenRepository.findValidRefreshToken(userId)
    }

    fun validateToken(token: String?): Boolean {
        try {
            parser.parseSignedClaims(token)
            return true
        } catch (e: JwtException) {
            log.error(e) { "유효하지 않은 토큰입니다." }
        } catch (e: IllegalStateException) {
            log.error(e) { "이상한 토큰입니다." }
        } catch (e: Exception) {
            log.error(e) { "완전히 이상한 토큰입니다." }
        }
        return false
    }

    fun parseToken(token: String): TokenBody {
        val claimsJws = parser.parseSignedClaims(token)

        val sub = claimsJws.payload.subject
        val role = claimsJws.payload["role"] as String

        return TokenBody(
            userId = sub.toLong(),
            role = role
        )
    }

    private fun issueToken(id: Long, role: Role, expiration: Long): String {
        return Jwts.builder()
            .subject(id.toString())
            .claim("role", role.name)
            .issuedAt(Date())
            .expiration(Date(Date().time + expiration))
            .signWith(this.secretKey, SIG.HS256)
            .compact()
    }
}
