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

package org.springframework.context.event

import kotlinx.coroutines.runBlocking
import org.springframework.context.ApplicationEvent
import org.springframework.kotlin.experimental.coroutine.event.CoroutineApplicationListener
import org.springframework.kotlin.experimental.coroutine.event.CoroutineApplicationEventPublisher
import org.springframework.kotlin.experimental.coroutine.event.CoroutineEvent
import org.springframework.util.ObjectUtils
import java.lang.reflect.Method
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal open class SimpleCoroutineApplicationListenerMethodAdapter(beanName: String,
    targetClass: Class<*>, method: Method, val publisher: CoroutineApplicationEventPublisher
): AbstractCoroutineApplicationListenerMethodAdapter(beanName, targetClass, method),
        CoroutineApplicationListener<CoroutineEvent<ApplicationEvent>> {

    override fun processEvent(event: ApplicationEvent) = runBlocking {
        processApplicationEvent(event)
    }

    override suspend fun onCoroutineApplicationEvent(event: CoroutineEvent<ApplicationEvent>) =
            processApplicationEvent(event.payload)

    /**
     * Process the specified [ApplicationEvent], checking if the condition
     * match and handling non-null result, if any.
     */
    suspend private fun processApplicationEvent(event: ApplicationEvent) {
        val args = resolveArguments(event)
        if (shouldHandle(event, args)) {
            val result = suspendCoroutine<Any?> { cont ->
                val result = try {
                    doInvoke(*(args + cont))
                } catch (e: Throwable) {
                    cont.resumeWithException(e)
                    COROUTINE_SUSPENDED
                }

                if (result !== COROUTINE_SUSPENDED) {
                    cont.resume(result)
                }
            }

            if (result != null && result !== Unit) {
                handleResult(result)
            } else {
                logger.trace("No result object given - no result to handle")
            }
        }
    }

    suspend private fun handleResult(result: Any) {
        if (result.javaClass.isArray) {
            val events = ObjectUtils.toObjectArray(result)
            for (event in events) {
                publishEvent(event)
            }
        } else if (result is Collection<*>) {
            for (event in result) {
                publishEvent(event)
            }
        } else {
            publishEvent(result)
        }
    }

    suspend private fun publishEvent(event: Any?) {
        if (event != null) {
            publisher.publishEvent(event)
        }
    }
}
