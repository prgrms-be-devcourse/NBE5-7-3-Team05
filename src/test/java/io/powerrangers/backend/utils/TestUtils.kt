package io.powerrangers.backend.utils

import io.powerrangers.backend.dto.UserFollowResponseDto
import io.powerrangers.backend.entity.User

fun genUserFollowResList(size: Int) : List<UserFollowResponseDto> {
    var userList = mutableListOf<UserFollowResponseDto>()

    for(i in 0 until size) {
        userList.add(
            UserFollowResponseDto(
                id = i.toLong(),
                nickname = "user_$i",
                intro = "user_$i",
                profileImage = null
            )
        )
    }

    return userList
}