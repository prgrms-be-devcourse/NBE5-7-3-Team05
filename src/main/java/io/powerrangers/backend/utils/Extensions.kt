package io.powerrangers.backend.utils


import io.powerrangers.backend.entity.Comment
import io.powerrangers.backend.dto.*
import io.powerrangers.backend.entity.Task
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

fun User.toUserDetails(): UserDetails {
    return UserDetails(
        id = this.id,
        nickname = this.nickname,
        email = this.email,
        role = this.role,
        providerId = this.providerId,
        profileImage = this.profileImage
    )
}

fun Task.toTaskImageResponseDto(): TaskImageResponseDto {
    return TaskImageResponseDto(
        taskId = this.id!!,
        imageUrl = this.taskImage,
        status = this.status,
        dueDate = this.dueDate
    )
}

fun Task.toTaskResponseDto(): TaskResponseDto {
    return TaskResponseDto(
        id = this.id!!,
        category = this.category,
        content = this.content,
        dueDate = this.dueDate,
        status = this.status,
        taskImage = this.taskImage,
        scope = this.scope,
        nickname = this.user.nickname
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


