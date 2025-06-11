package io.powerrangers.backend.utils

import io.powerrangers.backend.dto.UserFollowResponseDto
import io.powerrangers.backend.entity.User

fun User.toUserFollowResponseDto(): UserFollowResponseDto {
    return UserFollowResponseDto(
        this.id,
        this.nickname,
        this.intro,
        this.profileImage,
    )
}