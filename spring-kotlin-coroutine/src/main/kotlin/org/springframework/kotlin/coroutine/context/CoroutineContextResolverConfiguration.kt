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

import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Role
import org.springframework.kotlin.coroutine.ConditionalOnClass
import org.springframework.kotlin.coroutine.context.resolver.DefaultCoroutineContextResolver
import org.springframework.kotlin.coroutine.context.resolver.ExecutorCoroutineContextResolver
import org.springframework.kotlin.coroutine.context.resolver.ReactorSchedulerCoroutineContextResolver
import org.springframework.kotlin.coroutine.context.resolver.Rx2SchedulerCoroutineContextResolver
import org.springframework.kotlin.coroutine.context.resolver.TaskSchedulerCoroutineContextResolver

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
open class CoroutineContextResolverConfiguration {
    @Bean
    open fun globalCoroutineContextResolver(
        beanFactory: BeanFactory, resolvers: Set<CoroutineContextResolver>): GlobalCoroutineContextResolver =
            DefaultGlobalCoroutineContextResolver(beanFactory, resolvers)

    @Bean
    open fun defaultCoroutineContextResolver(): CoroutineContextResolver =
            DefaultCoroutineContextResolver()

    @Bean
    open fun executorCoroutineContextResolver(): CoroutineContextResolver =
            ExecutorCoroutineContextResolver()

    @Bean
    @ConditionalOnClass("reactor.core.scheduler.Scheduler")
    open fun reactorSchedulerCoroutineContextResolver(): CoroutineContextResolver =
            ReactorSchedulerCoroutineContextResolver()

    @Bean
    @ConditionalOnClass("io.reactivex.Scheduler")
    open fun rx2SchedulerCoroutineContextResolver(): CoroutineContextResolver =
            Rx2SchedulerCoroutineContextResolver()

    @Bean
    open fun taskSchedulerCoroutineContextResolver(): CoroutineContextResolver =
            TaskSchedulerCoroutineContextResolver()
}

