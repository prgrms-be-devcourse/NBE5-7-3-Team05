package io.powerrangers.backend.utils

import io.powerrangers.backend.dto.Role
import io.powerrangers.backend.dto.TaskScope
import io.powerrangers.backend.dto.UserFollowResponseDto
import io.powerrangers.backend.entity.Task
import io.powerrangers.backend.entity.User
import java.time.LocalDateTime

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

fun genUser(
    targetNickname: String,
    targetProvider: String,
    targetProviderId: String,
    targetEmail: String,
): User = User(
    nickname = targetNickname,
    provider = targetProvider,
    providerId = targetProviderId,
    email = targetEmail,
)

fun genUser(nickname: String, email:String) =
    User(
        nickname = nickname,
        email = email,
        provider = "Test",
        providerId = "TestProviderId",
        followers = mutableListOf(),
        followings = mutableListOf()
    )

fun genTask(
    targetCategory: String,
    targetContent: String,
    targetDueDate: LocalDateTime,
    targetScope: TaskScope,
    targetUser: User
): Task = Task(
    category = targetCategory,
    content = targetContent,
    dueDate = targetDueDate,
    scope = targetScope,
    user = targetUser
)

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

fun genUserWithId(id: Long, nickname: String, email:String) =
    User(
        id = id,
        nickname = nickname,
        email = email,
        provider = "Test",
        providerId = "TestProviderId",
        followers = mutableListOf(),
        followings = mutableListOf()
    )

fun genUserListWithId(size: Int) : List<User> {
    val users = mutableListOf<User>()
    for(i in 0 until size) {
        users.add(genUserWithId(id = 10L + i, nickname="user_$i", email = "user_${i}@email.com"))
    }
    return users
}

fun genUserList(size: Int) : List<User> {
    val users = mutableListOf<User>()
    for(i in 0 until size) {
        users.add(genUser(nickname="user_$i", email = "user_${i}@email.com"))
    }
    return users
}

