package io.powerrangers.backend.dto

import jakarta.validation.constraints.NotNull

data class FollowRequestDto (
    @field: NotNull(message = "팔로잉 ID는 필수입니다.")
    val followingId: Long
)
