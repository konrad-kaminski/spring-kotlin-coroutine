package org.springframework.kotlin.experimental.coroutine.event

import org.apache.commons.logging.LogFactory
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.event.SimpleApplicationEventMulticaster
import org.springframework.core.ResolvableType

internal open class CoroutineApplicationEventMulticaster : SimpleApplicationEventMulticaster() {
    @Suppress("UNCHECKED_CAST")
    override fun multicastEvent(event: ApplicationEvent, eventType: ResolvableType?) =
            when (event) {
                is CoroutineEvent<*> -> multicastCoroutineEvent(event as CoroutineEvent<ApplicationEvent>, eventType)
                else                 -> super.multicastEvent(event, eventType)
            }

    private fun multicastCoroutineEvent(event: CoroutineEvent<ApplicationEvent>, eventType: ResolvableType?) {
        event.schedule {
            val type = eventType ?: ResolvableType.forInstance(event.payload)
            for (listener in getApplicationListeners(event, type)) {
                val executor = taskExecutor
                if (executor != null) {
                    executor.execute { invokeListener(listener, event) }
                } else {
                    invokeCoroutineListener(listener, event)
                }
            }
        }
    }

    private suspend fun invokeCoroutineListener(listener: ApplicationListener<*>, event: CoroutineEvent<ApplicationEvent>) {
        val onApplicationEventInvoker = listener.getOnApplicationEventInvoker<CoroutineEvent<ApplicationEvent>>()

        if (errorHandler != null) {
            try {
                onApplicationEventInvoker.invoke(event)
            } catch (err: Throwable) {
                errorHandler.handleError(err)
            }

        } else {
            try {
                onApplicationEventInvoker.invoke(event)
            } catch (ex: ClassCastException) {
                val msg = ex.message
                if (msg == null || msg.startsWith(event.javaClass.name)) {
                    // Possibly a lambda-defined listener which we could not resolve the generic event type for
                    val logger = LogFactory.getLog(javaClass)
                    if (logger.isDebugEnabled) {
                        logger.debug("Non-matching event type for listener: " + listener, ex)
                    }
                } else {
                    throw ex
                }
            }

        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun <E : CoroutineEvent<ApplicationEvent>> ApplicationListener<*>.getOnApplicationEventInvoker(): suspend (E) -> Unit =
        when (this) {
            is CoroutineApplicationListener<*> -> { event -> (this as CoroutineApplicationListener<E>).onCoroutineApplicationEvent(event) }
            else                               -> { event -> (this as ApplicationListener<E>).onApplicationEvent(event) }
        }
