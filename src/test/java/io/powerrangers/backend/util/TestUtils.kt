package io.powerrangers.backend.util

import io.powerrangers.backend.dto.TaskScope
import io.powerrangers.backend.entity.Task
import io.powerrangers.backend.entity.User
import java.time.LocalDateTime

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

fun genTask(
    targetCategory: String,
    targetContent: String,
    targetdueDate: LocalDateTime,
    targetScope: TaskScope,
    targetUser: User
): Task = Task(
    category = targetCategory,
    content = targetContent,
    dueDate = targetdueDate,
    scope = targetScope,
    user = targetUser
)