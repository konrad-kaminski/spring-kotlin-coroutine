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

package demo.app.event

import demo.app.util.logger
import demo.app.web.DemoApplicationEvent
import demo.app.web.SimpleEvent
import kotlinx.coroutines.delay
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
open class CoroutineListener {
    @EventListener
    suspend open fun handle(event: SimpleEvent) {
        delay(10)
        logger.info ("Received event $event")
    }

    @EventListener
    suspend open fun handleDemoEvent(event: DemoApplicationEvent) {
        delay(10)
        logger.info ("Received demoEvent $event")
    }

    companion object {
        private val logger = logger()
    }
}
