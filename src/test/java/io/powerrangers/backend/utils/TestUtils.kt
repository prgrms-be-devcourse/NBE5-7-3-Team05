package io.powerrangers.backend.utils

import io.powerrangers.backend.entity.User

fun genUser(nickname: String, email:String) =
    User(
        nickname = nickname,
        email = email,
        provider = "Test",
        providerId = "TestProviderId",
        followers = mutableListOf(),
        followings = mutableListOf()
    )

fun genUserList(size: Int) : List<User> {
    val users = mutableListOf<User>()
    for(i in 0 until size) {
        users.add(genUser(nickname="user_$i", email = "user_${i}@email.com"))
    }
    return users
}