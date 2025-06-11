package io.powerrangers.backend.utils

import io.powerrangers.backend.dto.UserFollowResponseDto
import io.powerrangers.backend.entity.User

fun User.toUserFollowResponseDto(): UserFollowResponseDto {
    return UserFollowResponseDto(
        id = this.id,
        nickname = this.nickname,
        intro = this.intro,
        profileImage = this.profileImage,
    )
}