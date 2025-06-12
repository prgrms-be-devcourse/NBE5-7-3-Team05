package io.powerrangers.backend.util

import io.powerrangers.backend.dto.Role
import io.powerrangers.backend.entity.User

fun genUser(
    targetNickname: String,
    targetProfileImage: String?,
    targetProvider: String,
    targetProviderId: String,
    targetEmail: String,
    targetIntro: String?,
    targetRole: Role = Role.USER
): User = User(
    nickname = targetNickname,
    profileImage = targetProfileImage,
    provider = targetProvider,
    providerId = targetProviderId,
    email = targetEmail,
    intro = targetIntro,
    role = targetRole
)