package io.powerrangers.backend.dto

data class FollowCheckResponseDto (
    val userId: Long,
    val following : Boolean = false
)