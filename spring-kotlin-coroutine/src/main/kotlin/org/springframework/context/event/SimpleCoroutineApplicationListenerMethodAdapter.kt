package org.springframework.context.event

import kotlinx.coroutines.experimental.runBlocking
import org.springframework.context.ApplicationEvent
import org.springframework.kotlin.experimental.coroutine.event.CoroutineApplicationListener
import org.springframework.kotlin.experimental.coroutine.event.CoroutineApplicationEventPublisher
import org.springframework.kotlin.experimental.coroutine.event.CoroutineEvent
import org.springframework.util.ObjectUtils
import java.lang.reflect.Method
import kotlin.coroutines.experimental.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.experimental.suspendCoroutine

internal open class SimpleCoroutineApplicationListenerMethodAdapter(beanName: String,
    targetClass: Class<*>, method: Method, val publisher: CoroutineApplicationEventPublisher
): AbstractCoroutineApplicationListenerMethodAdapter(beanName, targetClass, method),
        CoroutineApplicationListener<CoroutineEvent<ApplicationEvent>> {

    override fun processEvent(event: ApplicationEvent) = runBlocking {
        processApplicationEvent(event)
    }

    suspend override fun onCoroutineApplicationEvent(event: CoroutineEvent<ApplicationEvent>) =
            processApplicationEvent(event.payload)

    /**
     * Process the specified [ApplicationEvent], checking if the condition
     * match and handling non-null result, if any.
     */
    suspend fun processApplicationEvent(event: ApplicationEvent) {
        val args = resolveArguments(event)
        if (shouldHandle(event, args)) {
            val result = suspendCoroutine<Any?> { cont ->
                val result = try {
                    doInvoke(*(args + cont))
                } catch (e: Throwable) {
                    cont.resumeWithException(e)
                    COROUTINE_SUSPENDED
                }

                if (result != COROUTINE_SUSPENDED) {
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

    suspend protected fun handleResult(result: Any) {
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