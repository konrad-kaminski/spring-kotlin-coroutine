package org.springframework.kotlin.experimental.coroutine.event

import org.springframework.beans.factory.Aware

interface CoroutineApplicationEventPublisherAware : Aware {
    fun setCoroutineApplicationEventPublisher(publisher: CoroutineApplicationEventPublisher)
}

