package io.powerrangers.backend.dto

data class FollowResponseDto (
    val followId: Long,
    val followerId: Long,
    val followingId: Long,
)
