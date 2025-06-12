package io.powerrangers.backend.dto.comment

import io.powerrangers.backend.entity.Comment
import java.time.LocalDateTime


data class CommentResponseDto (
    val id: Long,
    val content: String,
    val nickname: String,
    val profileImage: String?,
    val children: List<CommentResponseDto> = emptyList(),
    val createdAt: LocalDateTime,
    val userId: Long
) {
    companion object {
        fun from(comment: Comment): CommentResponseDto {
            return CommentResponseDto(
                id=comment.id!!,
                content = comment.content,
                nickname = comment.user.nickname,
                profileImage = comment.user.profileImage,
                createdAt = comment.createdAt,
                userId = comment.user.id!!,
                children = emptyList()
            )
        }
    }
}