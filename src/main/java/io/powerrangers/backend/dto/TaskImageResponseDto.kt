package io.powerrangers.backend.dto

import io.powerrangers.backend.entity.Task
import java.time.LocalDateTime

data class TaskImageResponseDto(
    val taskId: Long?,
    val imageUrl: String?,
    val status: TaskStatus,
    val dueDate: LocalDateTime?
) {
    companion object {
        fun from(task: Task): TaskImageResponseDto {
            return TaskImageResponseDto(
                taskId = task.id,
                imageUrl = task.taskImage,
                status = task.status,
                dueDate = task.dueDate
            )
        }
    }
}
