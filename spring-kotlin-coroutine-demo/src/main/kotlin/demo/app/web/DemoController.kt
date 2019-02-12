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

package demo.app.web

import demo.app.service.DemoService
import demo.app.util.logger
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.kotlin.coroutine.annotation.Coroutine
import org.springframework.kotlin.coroutine.context.DEFAULT_DISPATCHER
import org.springframework.kotlin.coroutine.event.CoroutineApplicationEventPublisher
import org.springframework.kotlin.coroutine.web.client.CoroutineRestOperations
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
open class DemoController(
        private val demoService: DemoService,
        private val publisher: ApplicationEventPublisher,
        private val coroutinePublisher: CoroutineApplicationEventPublisher
) {
    private val restOperations = CoroutineRestOperations()

    @GetMapping("/delayed")
    suspend open fun delayedReturn(): String {
        logger.info ("Before call to [demoService.delayed]")
        val result = demoService.delayedReturn("delayed", 1000)
        logger.info ("After call to [demoService.delayed]")

        return result
    }

    @GetMapping("/defaultDispatcher")
    suspend open fun defaultDispatcherReturn(): String {
        logger.info ("Before call to [demoService.defaultDispatcherReturn]")
        val result = demoService.defaultDispatcherReturn("defaultDispatcher")
        logger.info ("After call to [demoService.defaultDispatcherReturn]")

        return result
    }

    @GetMapping("/cachedDelayed")
    suspend open fun cachedDelayedReturn(): String {
        logger.info ("Before call to [demoService.cachedDelayedReturn]")
        val result = demoService.cachedDelayedReturn("cachedDelayed", 500)
        logger.info ("After call to [demoService.cachedDelayedReturn]")

        return result
    }

    @GetMapping("/cachedDefaultDispatcherDelayed")
    suspend open fun cachedDefaultDispatcherDelayed(): String {
        logger.info ("Before call to [demoService.cachedDefaultDispatcherDelayedReturn]")
        val result = demoService.cachedDefaultDispatcherDelayedReturn("cachedDefaultDsipatcherDelayed", 1500)
        logger.info ("After call to [demoService.cachedDefaultDeispatcherDelayedReturn]")

        return result
    }

    @GetMapping("/defaultDispatcherController")
    @Coroutine(DEFAULT_DISPATCHER)
    suspend open fun defaultDispatcherController(): String {
        logger.info ("In [defaultDispatcherController]")

        return "defaultDispatcherController"
    }

    @GetMapping("/event")
    suspend open fun event(): String {
        publisher.publishEvent(SimpleEvent("Hello"))
        publisher.publishEvent(DemoApplicationEvent(this, "Hello"))
        coroutinePublisher.publishEvent(SimpleEvent("Hello-coroutine"))
        coroutinePublisher.publishEvent(DemoApplicationEvent(this, "Hello-coroutine"))

        return "event"
    }

    @GetMapping("/rest")
    suspend open fun rest(request: HttpServletRequest): String {
        logger.info ("Before call to [restOperations.getForEntity]")
        val result = restOperations.getForEntity(request.requestURL.toString().replace("rest", "delayed"), String::class.java)
        logger.info ("After call to [restOperations.getForEntity]")

        return "Rest result: ${result.body}"
    }

    override fun toString(): String = "DemoController"

    companion object {
        private val logger = logger()
    }
}

open class DemoApplicationEvent(
    source: Any,
    val message: String
): ApplicationEvent(source) {

    override fun toString(): String = "DemoApplicationEvent(source=$source, message=$message)"
}

data class SimpleEvent(
    val message: String
)