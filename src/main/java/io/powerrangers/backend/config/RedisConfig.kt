package io.powerrangers.backend.config

import io.powerrangers.backend.dao.adapter.RedisSubscriber
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun redisTemplate(factory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(factory)
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = GenericJackson2JsonRedisSerializer()
        return template
    }

    @Bean
    fun container(
        factory: RedisConnectionFactory,
        listenerAdapter: MessageListenerAdapter
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(factory)
        container.addMessageListener(listenerAdapter, topic())
        return container
    }

    @Bean
    fun listenerAdapter(subscriber: RedisSubscriber): MessageListenerAdapter{
        return MessageListenerAdapter(subscriber, "onMessage")
    }

    @Bean
    fun topic(): ChannelTopic {
        return ChannelTopic("notify-channel")
    }
}