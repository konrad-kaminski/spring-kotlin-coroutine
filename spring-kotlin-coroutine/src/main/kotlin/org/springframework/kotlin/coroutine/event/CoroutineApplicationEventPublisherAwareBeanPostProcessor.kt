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

package org.springframework.kotlin.coroutine.event

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ConfigurableApplicationContext
import java.security.AccessController
import java.security.PrivilegedAction

internal open class CoroutineApplicationEventPublisherAwareBeanPostProcessor(
        private val applicationContext: ConfigurableApplicationContext,
        private val publisher: CoroutineApplicationEventPublisher
) : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any = bean.apply {
        if (this is CoroutineApplicationEventPublisherAware) {
            val acc = applicationContext.beanFactory.accessControlContext

            if (acc != null) {
                AccessController.doPrivileged(PrivilegedAction {
                    setCoroutineApplicationEventPublisher(publisher)
                }, acc)
            } else {
                setCoroutineApplicationEventPublisher(publisher)
            }
        }
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = bean
}
