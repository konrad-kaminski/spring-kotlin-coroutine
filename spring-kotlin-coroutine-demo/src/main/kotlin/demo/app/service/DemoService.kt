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

package demo.app.service

import demo.app.util.logger
import kotlinx.coroutines.delay
import org.springframework.cache.annotation.Cacheable
import org.springframework.kotlin.coroutine.annotation.Coroutine
import org.springframework.kotlin.coroutine.context.DEFAULT_DISPATCHER
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
open class DemoService {
    @Scheduled(cron = "0 0 0 1 1 *")
    suspend open fun newYear() {
        logger.info("Happy New Year!")
    }

    @Scheduled(fixedRate = 60_000)
    suspend open fun everyMinute() {
        logger.info("I'm still alive...")
    }

    suspend open fun delayedReturn(s: String, delayMillis: Long): String {
        logger.info ("Before delay in [delayedReturn]")
        delay(delayMillis)
        logger.info ("After delay in [delayedReturn]")

        return s
    }

    @Coroutine(DEFAULT_DISPATCHER)
    suspend open fun defaultDispatcherReturn(s: String): String {
        logger.info ("In [defaultDispatcher]")

        return s
    }

    @Cacheable("cache1")
    suspend open fun cachedDelayedReturn(s: String, delayMillis: Long): String {
        logger.info ("Before delay in [cachedDelayedReturn]")
        delay(delayMillis)
        logger.info ("After delay in [cachedDelayedReturn]")

        return s
    }

    @Cacheable("cache2")
    @Coroutine(DEFAULT_DISPATCHER)
    suspend open fun cachedDefaultDispatcherDelayedReturn(s: String, delayMillis: Long): String {
        logger.info ("Before delay in [cachedDefaultDispatcherDelayedReturn]")
        delay(delayMillis)
        logger.info ("After delay in [cachedDefaultDispatcherDelayedReturn]")

        return s
    }

    companion object {
        private val logger = logger()
    }
}
