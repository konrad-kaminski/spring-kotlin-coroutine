/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.kotlin.coroutine.event

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
            else                               -> { event -> (this as ApplicationListener<ApplicationEvent>).onApplicationEvent(event.payload) }
        }
