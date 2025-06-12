package io.powerrangers.backend.dto

import io.powerrangers.backend.entity.Task
import java.time.LocalDateTime

data class TaskImageResponseDto(
    val taskId: Long,
    val imageUrl: String?,
    val status: TaskStatus,
    val dueDate: LocalDateTime?
)
