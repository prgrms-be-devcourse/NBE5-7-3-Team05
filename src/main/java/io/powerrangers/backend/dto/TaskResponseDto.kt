package io.powerrangers.backend.dto

import java.time.LocalDateTime
import io.powerrangers.backend.entity.Task

data class TaskResponseDto(
    val id: Long?,
    val category: String,
    val content: String,
    val dueDate: LocalDateTime?,
    val status: TaskStatus,
    val taskImage: String?,
    val scope: TaskScope,
    val nickname: String
) {
    companion object {
        fun from(task: Task): TaskResponseDto {
            return TaskResponseDto(
                id = task.id,
                category = task.category,
                content = task.content,
                dueDate = task.dueDate,
                status = task.status,
                taskImage = task.taskImage,
                scope = task.scope,
                nickname = task.user.nickname
            )
        }
    }
}


