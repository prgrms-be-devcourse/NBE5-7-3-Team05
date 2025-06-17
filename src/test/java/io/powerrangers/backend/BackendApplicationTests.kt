package io.powerrangers.backend

import io.powerrangers.backend.dao.adapter.RedisSubscriber
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
internal class BackendApplicationTests {
    @MockitoBean
    lateinit var redisMessageListenerContainer: RedisMessageListenerContainer

    @MockitoBean
    lateinit var redisSubscriber: RedisSubscriber

    @Test
    fun contextLoads() {
    }
}
