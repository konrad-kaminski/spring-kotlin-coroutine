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

import kotlinx.coroutines.experimental.delay
import org.springframework.cache.annotation.Cacheable
import demo.app.util.logger
import org.springframework.kotlin.experimental.coroutine.annotation.Coroutine
import org.springframework.kotlin.experimental.coroutine.context.COMMON_POOL
import org.springframework.stereotype.Component

@Component
open class DemoService {
    suspend open fun delayedReturn(s: String, delayMillis: Long): String {
        logger.info ("Before delay in [delayedReturn]")
        delay(delayMillis)
        logger.info ("After delay in [delayedReturn]")

        return s
    }

    @Coroutine(COMMON_POOL)
    suspend open fun commmonPoolReturn(s: String): String {
        logger.info ("In [commmonPoolReturn]")

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
    @Coroutine(COMMON_POOL)
    suspend open fun cachedCommonPoolDelayedReturn(s: String, delayMillis: Long): String {
        logger.info ("Before delay in [cachedCommonPoolDelayedReturn]")
        delay(delayMillis)
        logger.info ("After delay in [cachedCommonPoolDelayedReturn]")

        return s
    }

    companion object {
        private val logger = logger()
    }
}