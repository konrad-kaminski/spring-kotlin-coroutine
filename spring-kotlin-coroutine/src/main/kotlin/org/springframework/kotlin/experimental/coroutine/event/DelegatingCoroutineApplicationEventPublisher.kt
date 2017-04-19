package org.springframework.kotlin.experimental.coroutine.event

import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.PayloadApplicationEvent

internal open class DelegatingCoroutineApplicationEventPublisher(
        private val delegate: ApplicationEventPublisher
) : CoroutineApplicationEventPublisher {
    suspend override fun publishEvent(event: ApplicationEvent) = doPublishEvent(event)

    suspend override fun publishEvent(event: Any) = doPublishEvent(PayloadApplicationEvent(this, event))

    suspend private fun doPublishEvent(event: ApplicationEvent): Unit = doPublishEvents(listOf(event))

    suspend private fun doPublishEvents(events: List<ApplicationEvent>): Unit =
        events.forEach { event ->
            val coroutineEvent = CoroutineEvent(event, this)

            delegate.publishEvent(coroutineEvent)

            coroutineEvent.invokeBlocks()
        }
}
