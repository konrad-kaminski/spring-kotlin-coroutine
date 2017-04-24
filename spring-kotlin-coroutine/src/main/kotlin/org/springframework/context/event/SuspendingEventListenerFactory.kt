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

package org.springframework.context.event

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.kotlin.experimental.coroutine.event.CoroutineApplicationEventPublisher
import java.lang.reflect.Method

private val evaluator = EventExpressionEvaluator()

internal fun createApplicationListener(applicationContext: ApplicationContext,
    beanName: String, type: Class<*>, method: Method, publisher: CoroutineApplicationEventPublisher
): ApplicationListener<*> =
        SimpleCoroutineApplicationListenerMethodAdapter(beanName, type, method, publisher).apply {
            init(applicationContext, evaluator)
        }