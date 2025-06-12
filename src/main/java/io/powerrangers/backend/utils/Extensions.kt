package io.powerrangers.backend.utils

import io.powerrangers.backend.dto.UserDetails
import io.powerrangers.backend.dto.UserFollowResponseDto
import io.powerrangers.backend.dto.UserGetProfileResponseDto
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
        name = this.nickname,
        email = this.email,
        role = this.role,
        providerId = this.providerId,
        profileImage = this.profileImage
    )
}
