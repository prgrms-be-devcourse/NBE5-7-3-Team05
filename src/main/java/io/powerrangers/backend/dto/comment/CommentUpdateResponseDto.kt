package io.powerrangers.backend.dto.comment

import io.powerrangers.backend.entity.Comment


data class CommentUpdateResponseDto (
    val id: Long,
    val content: String,
    val nickname: String,
    val profileImage: String?
){
    companion object {
        fun from(comment: Comment): CommentUpdateResponseDto {
            return CommentUpdateResponseDto(
                id=comment.id!!,
                content=comment.content,
                nickname=comment.user.nickname,
                profileImage = comment.user.profileImage
            )
        }
    }
}
