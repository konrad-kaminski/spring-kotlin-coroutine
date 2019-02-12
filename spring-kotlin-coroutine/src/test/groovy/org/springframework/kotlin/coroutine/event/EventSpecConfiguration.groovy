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

import org.springframework.context.annotation.Bean
import org.springframework.context.event.EventListener

import java.util.function.Function

class EventSpecConfiguration {
    @Bean
    CoroutineListener coroutineListener() {
        return new CoroutineListener()
    }

    @Bean
    RegularListener regularListener() {
        return new RegularListener()
    }
}

class RegularListener {
    private def events = new Stack<Object>()
    private def testEvents = new Stack<Object>()

    private Function<Object, Object> listenMock = { null }

    @EventListener
    Object listen(Object event) {
        events.push (event)

        def res = listenMock.apply(event)
        return res
    }

    @EventListener
    Object listen(TestEvent event) {
        testEvents.push (event)

        def res = listenMock.apply(event)
        return res
    }

    void reset(Function<Object, Object> mock) {
        events.clear()
        testEvents.clear()
        listenMock = mock
    }

    Stack<Object> getEvents() {
        return events
    }

    Stack<Object> getTestEvents() {
        return testEvents
    }
}

