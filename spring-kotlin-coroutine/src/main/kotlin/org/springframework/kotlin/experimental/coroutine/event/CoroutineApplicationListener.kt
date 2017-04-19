package org.springframework.kotlin.experimental.coroutine.event

import org.springframework.context.ApplicationEvent

internal interface CoroutineApplicationListener<in E : CoroutineEvent<ApplicationEvent>> : java.util.EventListener {
    suspend fun onCoroutineApplicationEvent(event: E)
}
