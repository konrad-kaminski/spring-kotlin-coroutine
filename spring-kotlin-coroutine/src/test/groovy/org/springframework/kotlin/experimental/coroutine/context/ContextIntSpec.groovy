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

package org.springframework.kotlin.experimental.coroutine.context

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kotlin.experimental.coroutine.IntSpecConfiguration
import spock.lang.Specification

import static org.springframework.kotlin.experimental.coroutine.TestUtilsKt.runBlocking

@SpringBootTest(classes = [IntSpecConfiguration, ContextConfiguration])
class ContextIntSpec extends Specification {
    @Autowired
    private CustomContextService customContextService

    def "should use ForkJoinPool within COMMON_POOL context"() {
        when:
        def thread = runBlocking { cont ->
            customContextService.commonPoolFun(cont)
        }

        then:
        thread.name =~ /^ForkJoinPool\.commonPool-worker/
    }

    def "should use same thread within UNCONFINED context"() {
        when:
        def thread = runBlocking { cont ->
            customContextService.unconfinedPoolFun(cont)
        }

        then:
        thread == Thread.currentThread()
    }

    def "should use Rx2 io scheduler thread when using context from Rx2 io scheduler"() {
        when:
        def thread = runBlocking { cont ->
            customContextService.rx2IoSchedulerFun(cont)
        }

        then:
        thread.name =~ /^RxCachedThreadScheduler/
    }

    def "should use Executor thread when using context from Executor"() {
        when:
        def thread = runBlocking { cont ->
            customContextService.singleTestExecutorFun(cont)
        }

        then:
        thread.name == "SingleTest"
    }
}
