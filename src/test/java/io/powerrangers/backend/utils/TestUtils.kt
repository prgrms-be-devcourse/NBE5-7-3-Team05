package io.powerrangers.backend.utils

import io.powerrangers.backend.entity.User

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

fun genUser(nickname: String, email:String) =
    User(
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

