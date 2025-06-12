package io.powerrangers.backend.utils

import io.powerrangers.backend.dto.UserDetails
import io.powerrangers.backend.entity.User

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