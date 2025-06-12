package io.powerrangers.backend.dto

data class FollowCountResponseDto (
    val userId: Long,
    val followerCount: Long,
    val followingCount: Long
)