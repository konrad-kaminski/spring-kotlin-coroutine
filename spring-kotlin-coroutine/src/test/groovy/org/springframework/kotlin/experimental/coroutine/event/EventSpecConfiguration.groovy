package org.springframework.kotlin.experimental.coroutine.event

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

