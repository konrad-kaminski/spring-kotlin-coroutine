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

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.Unconfined
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Role

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
internal open class CoroutineContexts {
    @Bean(COMMON_POOL)
    open fun commonPool() = CommonPool

    @Bean(DEFAULT_DISPATCHER)
    open fun defaultDispatcher() = DefaultDispatcher

    @Bean(UNCONFINED)
    open fun unconfined() = Unconfined
}

const val COMMON_POOL = "CommonPool"
const val DEFAULT_DISPATCHER = "DefaultDispatcher"
const val UNCONFINED = "Unconfined"
