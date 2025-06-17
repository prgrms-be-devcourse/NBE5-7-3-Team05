package io.powerrangers.backend

import io.powerrangers.backend.dao.adapter.RedisSubscriber
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
internal class BackendApplicationTests {

    @Mock
    lateinit var redisMessageListenerContainer: RedisMessageListenerContainer

    @Mock
    lateinit var redisSubscriber: RedisSubscriber
    @Test
    fun contextLoads() {
    }
}
