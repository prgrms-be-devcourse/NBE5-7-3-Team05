package io.powerrangers.backend.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Notification(
    val receiverId: Long,
    val type: NotificationType,
    val content: String
)