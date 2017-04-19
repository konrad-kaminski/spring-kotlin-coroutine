package org.springframework.kotlin.experimental.coroutine.event

import org.springframework.context.ApplicationEvent

interface CoroutineApplicationEventPublisher {
    suspend fun publishEvent(event: ApplicationEvent)

    suspend fun publishEvent(event: Any)
}
