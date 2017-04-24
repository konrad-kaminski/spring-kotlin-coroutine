package org.springframework.kotlin.experimental.coroutine.event

import kotlinx.coroutines.experimental.delay
import org.springframework.context.event.EventListener
import java.util.Stack
import java.util.function.Function

open class CoroutineListener {
    private val events = Stack<Any>()
    private val testEvents = Stack<Any>()
    private var listenMock = Function<Any, Any?> { _ -> null }

    @EventListener
    suspend open fun listen(event: Any): Any? {
        events.push(event)
        delay(1)

        return listenMock.apply(event)
    }

    @EventListener
    suspend open fun listen(event: TestEvent): Any? {
        testEvents.push(event)

        return listenMock.apply(event)
    }

    open fun reset(mock: Function<Any, Any?>) {
        events.clear()
        testEvents.clear()
        listenMock = mock
    }

    open fun getEvents() = events

    open fun getTestEvents() = testEvents
}