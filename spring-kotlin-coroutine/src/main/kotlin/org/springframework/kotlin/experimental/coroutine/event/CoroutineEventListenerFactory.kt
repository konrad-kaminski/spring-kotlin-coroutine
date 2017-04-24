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

package org.springframework.kotlin.experimental.coroutine.event

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.context.event.EventListener
import org.springframework.context.event.EventListenerFactory
import org.springframework.context.event.createApplicationListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.kotlin.experimental.coroutine.isSuspend
import java.lang.reflect.Method

internal open class CoroutineEventListenerFactory(
    private val applicationContext: ApplicationContext,
    private val eventPublisher: CoroutineApplicationEventPublisher
) : EventListenerFactory, Ordered {
    override fun getOrder() = Ordered.LOWEST_PRECEDENCE - 1

    override fun supportsMethod(method: Method): Boolean =
            AnnotationUtils.findAnnotation(method, EventListener::class.java) != null &&
                    method.isSuspend

    override fun createApplicationListener(beanName: String, type: Class<*>, method: Method): ApplicationListener<*> =
            createApplicationListener(applicationContext, beanName, type, method, eventPublisher)
}

