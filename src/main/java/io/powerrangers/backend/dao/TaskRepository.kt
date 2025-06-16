package io.powerrangers.backend.dao

import io.powerrangers.backend.entity.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface TaskRepository : JpaRepository<Task, Long> {

    fun findAllByUserId(userId: Long): List<Task>

    @Query("select t from Task t join t.user u where u.id = :userId and t.scope != 'PRIVATE'")
    fun findTasksForFollowers(@Param("userId") userId: Long): List<Task>

    @Query("select t from Task t join t.user u where u.id = :userId and t.scope = 'PUBLIC'")
    fun findTasksForPublic(@Param("userId") userId: Long): List<Task>

    @Query("""
    SELECT CAST(t.dueDate AS date), COUNT(t)
    FROM Task t
    WHERE t.user.id = :targetUserId
      AND t.dueDate BETWEEN :start AND :end
      AND (
          (:scope = 'PUBLIC' AND t.scope = 'PUBLIC')
          OR (:scope = 'FOLLOWERS' AND (t.scope = 'PUBLIC' OR t.scope = 'FOLLOWERS'))
          OR (:scope = 'PRIVATE' AND t.user.id = :currentUserId)
      )
    GROUP BY CAST(t.dueDate AS date)
""")
    fun countTasksByDateWithScope(
        @Param("targetUserId") targetUserId: Long,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime,
        @Param("scope") scope: String,
        @Param("currentUserId") currentUserId: Long
    ): List<Array<Any>>

}