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

import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.PayloadApplicationEvent

internal open class DelegatingCoroutineApplicationEventPublisher(
        private val delegate: ApplicationEventPublisher
) : CoroutineApplicationEventPublisher {
    override suspend fun publishEvent(event: ApplicationEvent) = doPublishEvent(event)

    override suspend fun publishEvent(event: Any) = doPublishEvent(PayloadApplicationEvent(this, event))

    suspend private fun doPublishEvent(event: ApplicationEvent): Unit = doPublishEvents(listOf(event))

    suspend private fun doPublishEvents(events: List<ApplicationEvent>): Unit =
        events.forEach { event ->
            val coroutineEvent = CoroutineEvent(event, this)

            delegate.publishEvent(coroutineEvent)

            coroutineEvent.invokeBlocks()
        }
}
