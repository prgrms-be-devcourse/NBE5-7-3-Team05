package io.powerrangers.backend.dto

import java.time.LocalDateTime
import io.powerrangers.backend.entity.Task

data class TaskResponseDto(
    val id: Long,
    val category: String,
    val content: String,
    val dueDate: LocalDateTime,
    val status: TaskStatus,
    val taskImage: String?,
    val scope: TaskScope,
    val nickname: String
)


