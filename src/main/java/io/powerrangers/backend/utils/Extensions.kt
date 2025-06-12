package io.powerrangers.backend.utils

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
