package io.powerrangers.backend.dto

import java.time.LocalDateTime

data class CommentResponseDto (
    val id: Long,
    val content: String,
    val nickname: String,
    val profileImage: String?,
    val children: List<CommentResponseDto> = emptyList(),
    val createdAt: LocalDateTime,
    val userId: Long
)