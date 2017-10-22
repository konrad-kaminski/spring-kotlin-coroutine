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

import org.springframework.kotlin.experimental.coroutine.annotation.Coroutine
import java.lang.Thread.currentThread
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@Coroutine(COMMON_POOL)
annotation class CommonPoolCoroutine

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@Coroutine("ReactorScheduler")
annotation class ReactorSchedulerCoroutine

@ReactorSchedulerCoroutine
open class CustomContextService {
    suspend open fun defaultContextFun(): Thread = currentThread()

    @CommonPoolCoroutine
    suspend open fun metaCommonPoolFun(): Thread = currentThread()

    @Coroutine(COMMON_POOL)
    suspend open fun commonPoolFun(): Thread = currentThread()

    @Coroutine(UNCONFINED)
    suspend open fun unconfinedPoolFun(): Thread = currentThread()

    @Coroutine("ReactorScheduler")
    suspend open fun reactorSchedulerFun(): Thread = currentThread()

    @Coroutine("Rx1IoScheduler")
    suspend open fun rx1IoSchedulerFun(): Thread = currentThread()

    @Coroutine("Rx2IoScheduler")
    suspend open fun rx2IoSchedulerFun(): Thread = currentThread()

    @Coroutine("SingleTestExecutor")
    suspend open fun singleTestExecutorFun(): Thread = currentThread()

    @Coroutine(COMMON_POOL, name = "customCoroutineName")
    suspend open fun customCoroutineNameFun(): String = currentThread().name
}

@ReactorSchedulerCoroutine
interface ReactorSchedulerService

open class TestReactorSchedulerService: ReactorSchedulerService {
    suspend open fun contextFun(): Thread = currentThread()
}