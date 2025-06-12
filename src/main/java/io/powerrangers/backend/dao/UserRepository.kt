package io.powerrangers.backend.dao

import io.powerrangers.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?

    @Query("select u from User u where u.nickname LIKE :nickname%")
    fun findByNickname(nickname: String): List<User>
    fun existsByNickname(nickname: String): Boolean
}
