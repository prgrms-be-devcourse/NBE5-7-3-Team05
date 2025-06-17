package io.powerrangers.backend.service

import io.powerrangers.backend.dto.Notification
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service

@Service
class NotificationService (
    private val redisTemplate: RedisTemplate<String, Any>,
    private val topic: ChannelTopic
) {
    fun send(notification: Notification) {
        redisTemplate.convertAndSend(topic.topic, notification)
    }
}