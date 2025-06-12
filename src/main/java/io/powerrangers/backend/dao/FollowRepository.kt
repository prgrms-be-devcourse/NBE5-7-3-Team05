package io.powerrangers.backend.dao

import io.powerrangers.backend.entity.Follow
import io.powerrangers.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface FollowRepository : JpaRepository<Follow, Long> {
    fun existsByFollowerAndFollowing(follower: User, following: User): Boolean
    fun findByFollowerAndFollowing(follower: User, following: User): Follow?

    @Query("select u from Follow f JOIN f.follower u where f.following.id = :userId")
    fun findFollowersByUser(userId: Long): List<User>

    @Query("select u from Follow f JOIN f.following u where f.follower.id = :userId")
    fun findFollowingsByUser(userId: Long): List<User>

    @Query("select count(f) from Follow f where f.following.id = :userId")
    fun countFollowersByUser(userId: Long): Long

    @Query("select count(f) from Follow f where f.follower.id = :userId")
    fun countFollowingsByUser(userId: Long): Long
}
