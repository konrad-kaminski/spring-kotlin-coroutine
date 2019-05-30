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

package org.springframework.kotlin.coroutine.context

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kotlin.coroutine.IntSpecConfiguration
import spock.lang.Specification

import static org.springframework.kotlin.coroutine.TestUtilsKt.runBlocking

@SpringBootTest(classes = [IntSpecConfiguration, ContextConfiguration])
class ContextDebugIntSpec extends Specification {
    @Autowired
    private CustomContextService customContextService

    def "should include coroutine name in the thread name"() {
        when:
        def threadName = runBlocking { cont ->
            customContextService.customCoroutineNameFun(cont)
        }

        then:
        threadName =~ /@customCoroutineName#/
    }

}
