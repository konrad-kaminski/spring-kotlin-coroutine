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

package org.springframework.kotlin.experimental.coroutine.cache

import kotlinx.coroutines.experimental.delay
import org.springframework.cache.annotation.Cacheable

open class CachedService {
    private var counter = 0

    open fun resetCounter() {
        counter = 0
    }

    @Cacheable("cache1")
    suspend open fun cached(a: Int): Int = a + (counter++)

    @Cacheable("cache2")
    suspend open fun delayedCached(a: Int): Int {
        delay(1)

        return a + (counter++)
    }

    @Cacheable("cache3")
    open fun regularCached(a: Int): Int = a + (counter++)

    @Cacheable("cache4")
    suspend open fun throwingException(a: Int): Int =
        throw CacheTestException()

    @Cacheable("cache5")
    suspend open fun throwingExceptionOnCallback(a: Int): Int {
        delay(1)

        throw CacheTestException()
    }
}

open class CacheTestException: RuntimeException()