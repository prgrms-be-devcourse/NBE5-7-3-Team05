package io.powerrangers.backend.dto

import com.fasterxml.jackson.annotation.JsonFormat


@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class NotificationType {
    COMMENT,
    FOLLOW
}