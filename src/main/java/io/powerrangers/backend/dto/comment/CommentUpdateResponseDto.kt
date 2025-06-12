package io.powerrangers.backend.dto.comment

data class CommentUpdateResponseDto (
    val id: Long,
    val content: String,
    val nickname: String,
    val profileImage: String?
)
