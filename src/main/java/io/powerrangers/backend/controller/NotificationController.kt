package io.powerrangers.backend.controller

import io.powerrangers.backend.dao.adapter.RedisSubscriber
import io.powerrangers.backend.dto.Notification
import io.powerrangers.backend.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/notifications")
class NotificationController (
    private val notificationService: NotificationService
){
    private val log = LoggerFactory.getLogger(NotificationController::class.java)

    @CrossOrigin
    @GetMapping("/subscribe/{userId}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(@PathVariable userId: Long): SseEmitter {
        log.info("✅ SSE 구독 요청 들어옴: userId = $userId")

        val emitter = SseEmitter(60*60*1000L)
        RedisSubscriber.emitters[userId] = emitter

        emitter.onCompletion {
            RedisSubscriber.emitters.remove(userId)
            log.info("⛔ SSE 연결 종료: $userId")
        }

        emitter.onTimeout {
            RedisSubscriber.emitters.remove(userId)
            log.info("⏰ SSE 타임아웃: $userId")
        }

        try {
            // ✅ 연결 확인 메시지 보내기!
            emitter.send(SseEmitter.event()
                .name("connect")
                .data("SSE 연결 성공"))
        } catch (e: Exception) {
            log.error("❌ 초기 연결 메시지 전송 실패: ${e.message}")
        }

        return emitter
    }

    @PostMapping("/send")
    fun sendNotification(@RequestBody notification: Notification) {
        notificationService.send(notification)
    }
}