package io.powerrangers.backend.dto

data class UserGetProfileResponseDto(
    val userId: Long,
    val nickname: String,
    val intro: String?,
    val profileImage: String?
)


