package org.springframework.kotlin.experimental.coroutine.event

import org.springframework.context.ApplicationEvent

class TestEvent(source: Any): ApplicationEvent(source) {
    override fun equals(other: Any?): Boolean =
        other is TestEvent &&
        other.source == source &&
        other.timestamp == timestamp
}