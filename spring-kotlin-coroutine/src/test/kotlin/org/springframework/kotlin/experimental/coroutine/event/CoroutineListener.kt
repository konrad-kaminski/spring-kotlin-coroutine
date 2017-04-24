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