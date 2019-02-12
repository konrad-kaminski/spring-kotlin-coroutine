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

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportAware
import org.springframework.context.annotation.Role
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata
import org.springframework.kotlin.coroutine.EnableCoroutine
import io.reactivex.Scheduler as Rx2Scheduler
import reactor.core.scheduler.Scheduler as ReactorScheduler

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
internal open class ProxyCoroutineConfiguration : ImportAware {
    private lateinit var enableCoroutines: AnnotationAttributes

    override fun setImportMetadata(importMetadata: AnnotationMetadata) {
        enableCoroutines = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableCoroutine::class.java.name, false))
    }

    @Bean
    open fun coroutineAnnotationBeanPostProcessor(contextResolver: GlobalCoroutineContextResolver): CoroutineAnnotationBeanPostProcessor =
            CoroutineAnnotationBeanPostProcessor(contextResolver).apply {
                isProxyTargetClass = enableCoroutines.getBoolean("proxyTargetClass")
                order = enableCoroutines.getNumber("order")
            }
}
