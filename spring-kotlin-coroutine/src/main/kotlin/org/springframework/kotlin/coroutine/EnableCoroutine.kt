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

package org.springframework.kotlin.coroutine

import org.springframework.context.annotation.AdviceMode
import org.springframework.context.annotation.Import
import org.springframework.core.Ordered
import org.springframework.kotlin.coroutine.cache.CoroutineCacheConfiguration
import org.springframework.kotlin.coroutine.context.CoroutineConfigurationSelector
import org.springframework.kotlin.coroutine.context.CoroutineContextResolverConfiguration
import org.springframework.kotlin.coroutine.context.CoroutineContexts
import org.springframework.kotlin.coroutine.event.CoroutineEventSupportConfiguraton
import org.springframework.kotlin.coroutine.reactive.CoroutineReactiveAdapterRegistryConfiguration
import org.springframework.kotlin.coroutine.scheduler.CoroutineSchedulerConfiguration

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(CoroutineConfigurationSelector::class, CoroutineReactiveAdapterRegistryConfiguration::class,
        CoroutineContextResolverConfiguration::class, CoroutineContexts::class,
        CoroutineEventSupportConfiguraton::class, CoroutineCacheConfiguration::class,
        CoroutineSchedulerConfiguration::class)
annotation class EnableCoroutine(
        val proxyTargetClass: Boolean = false,
        val mode: AdviceMode = AdviceMode.PROXY,
        val order: Int = Ordered.LOWEST_PRECEDENCE,
        val schedulerDispatcher: String = ""
)
