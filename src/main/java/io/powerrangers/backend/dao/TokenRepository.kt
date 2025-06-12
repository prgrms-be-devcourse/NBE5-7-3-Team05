package io.powerrangers.backend.dao

import io.powerrangers.backend.entity.RefreshToken
import io.powerrangers.backend.entity.RefreshTokenBlackList
import io.powerrangers.backend.entity.User
import java.util.*

interface TokenRepository {
    fun save(user: User, refreshToken: String)
    fun tokenBlackList(refreshToken: String): Boolean
    fun addBlackList(refreshToken: RefreshToken): RefreshTokenBlackList?
    fun findValidRefreshToken(userId: Long): RefreshToken?
    fun findAllRefreshTokensByUserId(userId: Long): List<RefreshToken>
}
