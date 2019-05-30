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

package org.springframework.kotlin.coroutine.cache

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.kotlin.coroutine.IntSpecConfiguration
import spock.lang.Specification

import static org.springframework.kotlin.coroutine.TestUtilsKt.runBlocking

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

        cachedService.resetCounter()
    }

    def "should cache direct result"() {
        given:
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

    def "should cache callback result"() {
        given:
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

    def "should cache regular fun result"() {
        given:
        cachedService.regularCached(9)

        when:
        def value = cachedService.regularCached(9)

        then:
        value == 9
    }

    def "should receive exception thrown from direct call"() {
        when:
        runBlocking { cont ->
            cachedService.throwingException(11, cont)
        }

        then:
        thrown(CacheTestException)
    }

    def "should receive exception thrown from callback call"() {
        when:
        runBlocking { cont ->
            cachedService.throwingExceptionOnCallback(13, cont)
        }

        then:
        thrown(CacheTestException)
    }
}
