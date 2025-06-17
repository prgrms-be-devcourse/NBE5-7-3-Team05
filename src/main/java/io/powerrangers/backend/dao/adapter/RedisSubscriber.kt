package io.powerrangers.backend.dao.adapter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.powerrangers.backend.dto.Notification
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory

@Component
class RedisSubscriber : MessageListener {
    companion object{
        val emitters = ConcurrentHashMap<Long, SseEmitter>()
    }

    private val objectMapper = jacksonObjectMapper()
    private val log = LoggerFactory.getLogger(RedisSubscriber::class.java)

    override fun onMessage(message: Message, pattern: ByteArray) {
        val data = message.body.toString(Charsets.UTF_8)
        val notification = objectMapper.readValue(data, Notification::class.java)
        log.info("Redis알림수신: $notification")

        val emitter = emitters[notification.receiverId]
        if (emitter != null) {
            log.info("✅ emitter 존재함. 사용자 ID: ${notification.receiverId}")
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("notification")
                        .data(notification)
                )
                log.info("📤 SSE로 알림 전송 완료: $notification")
            } catch (e: Exception) {
                log.warn("❌ emitter 전송 실패, 제거됨. 사용자 ID: ${notification.receiverId}, 예외: ${e.message}")
                emitters.remove(notification.receiverId)
            }
        } else {
            log.warn("⚠️ emitter 없음. 사용자 ID: ${notification.receiverId} 는 아직 SSE 연결 안됨.")
        }
    }
}