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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.kotlin.experimental.coroutine.IntSpecConfiguration
import spock.lang.Specification

import static org.springframework.kotlin.experimental.coroutine.TestUtilsKt.runBlocking

@SpringBootTest(classes = [IntSpecConfiguration, CacheConfiguration])
class CacheIntSpec extends Specification {
    @Autowired
    private CachedService cachedService

    @Autowired
    private CacheManager cacheManager

    def setup() {
        cacheManager.cacheNames.forEach {
            cacheManager.getCache(it).clear()
        }
    }

    def "should cache direct return value"() {
        given:
        cachedService.resetCounter()
        runBlocking { cont ->
            cachedService.cached(1, cont)
        }

        when:
        def value = runBlocking { cont ->
            cachedService.cached(1, cont)
        }

        then:
        value == 1
    }

    def "should cache callback value"() {
        given:
        cachedService.resetCounter()
        runBlocking { cont ->
            cachedService.delayedCached(5, cont)
        }

        when:
        def value = runBlocking { cont ->
            cachedService.delayedCached(5, cont)
        }

        then:
        value == 5
    }
}
