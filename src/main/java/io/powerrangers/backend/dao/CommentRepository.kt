package io.powerrangers.backend.dao

import io.powerrangers.backend.entity.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CommentRepository : JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.task.id = :taskId")
    fun findByTaskId(@Param("taskId") taskId: Long): List<Comment?>
}