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

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportAware
import org.springframework.context.annotation.Role
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata
import org.springframework.kotlin.experimental.coroutine.EnableCoroutine
import org.springframework.kotlin.experimental.coroutine.context.resolver.DefaultCoroutineContextResolver
import org.springframework.kotlin.experimental.coroutine.context.resolver.ExecutorCoroutineContextResolver
import org.springframework.kotlin.experimental.coroutine.context.resolver.ReactorSchedulerCoroutineContextResolver
import org.springframework.kotlin.experimental.coroutine.context.resolver.Rx2SchedulerCoroutineContextResolver

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
internal open class ProxyCoroutineConfiguration : ImportAware {
    lateinit var enableCoroutines: AnnotationAttributes

    override fun setImportMetadata(importMetadata: AnnotationMetadata) {
        enableCoroutines = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableCoroutine::class.java.name, false))
    }

    @Bean
    open fun coroutineAnnotationBeanPostProcessor(resolvers: Set<CoroutineContextResolver>): CoroutineAnnotationBeanPostProcessor =
            CoroutineAnnotationBeanPostProcessor(resolvers).apply {
                isProxyTargetClass = enableCoroutines.getBoolean("proxyTargetClass")
                order = enableCoroutines.getNumber("order")
            }

    @Bean
    open fun defaultCoroutineContextResolver() = DefaultCoroutineContextResolver()

    @Bean
    open fun executorCoroutineContextesolver() = ExecutorCoroutineContextResolver()

    @Bean
    open fun reactorSchedulerCoroutineContextResolver() = ReactorSchedulerCoroutineContextResolver()

    @Bean
    open fun rx2SchedulerCoroutineContextResolver() = Rx2SchedulerCoroutineContextResolver()
}
