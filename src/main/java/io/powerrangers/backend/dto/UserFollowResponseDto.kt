package io.powerrangers.backend.dto

data class UserFollowResponseDto(
    val id: Long,
    val nickname: String,
    val intro: String?,
    val profileImage: String?
)

