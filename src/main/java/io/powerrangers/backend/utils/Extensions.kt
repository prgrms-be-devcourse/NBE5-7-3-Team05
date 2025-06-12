package io.powerrangers.backend.utils

import io.powerrangers.backend.dto.UserDetails
import io.powerrangers.backend.dto.UserFollowResponseDto
import io.powerrangers.backend.dto.UserGetProfileResponseDto
import io.powerrangers.backend.dto.comment.CommentResponseDto
import io.powerrangers.backend.dto.comment.CommentUpdateResponseDto
import io.powerrangers.backend.entity.Comment
import io.powerrangers.backend.entity.User

fun User.toProfileResponseDto(): UserGetProfileResponseDto {
    return UserGetProfileResponseDto(
        userId = this.id!!,
        nickname = this.nickname,
        intro = this.intro,
        profileImage = this.profileImage
    )
}

fun User.toUserFollowResponseDto(): UserFollowResponseDto {
    return UserFollowResponseDto(
        id = this.id!!,
        nickname = this.nickname,
        intro = this.intro,
        profileImage = this.profileImage
    )
}

fun User.toUserDetails(user: User): UserDetails {
    return UserDetails(
        id = user.id,
        name = user.nickname,
        email = user.email,
        role = user.role,
        providerId = user.providerId,
        profileImage = user.profileImage
    )
}

fun Comment.toResponseDto(children: List<CommentResponseDto> = emptyList()): CommentResponseDto {
    return CommentResponseDto(
        id = this.id!!,
        content = this.content,
        nickname = this.user.nickname,
        profileImage = this.user.profileImage,
        createdAt = this.createdAt,
        userId = this.user.id!!,
        children = children
    )
}

fun Comment.toUpdateResponseDto(): CommentUpdateResponseDto{
    return CommentUpdateResponseDto(
        id=this.id!!,
        content = this.content,
        nickname = this.user.nickname,
        profileImage = this.user.profileImage
    )
}


