/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.kotlin.coroutine.web

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.reactive.asPublisher
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Role
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.MethodParameter
import org.springframework.kotlin.coroutine.isSuspend
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerAdapter
import org.springframework.web.reactive.result.method.annotation.injectCustomControllerMethodResolver
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
open class CoroutinesWebFluxConfigurer(
    private val applicationContext: ApplicationContext
) : WebFluxConfigurer {
    @EventListener
    open fun init(event: ContextRefreshedEvent) {
        val adapter = applicationContext.getBean(RequestMappingHandlerAdapter::class.java)
        injectCustomControllerMethodResolver(adapter)
    }

    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        configurer.addCustomResolver(object: HandlerMethodArgumentResolver {
            override fun supportsParameter(parameter: MethodParameter) =
                    parameter.method.isSuspend && isContinuationClass(parameter.parameterType)

            override fun resolveArgument(parameter: MethodParameter, bindingContext: BindingContext,
                                         exchange: ServerWebExchange): Mono<Any> =
                CompletableFutureContinuation().let {
                    bindingContext.model.addAttribute("__continuation", AtomicReference(it))

                    Mono.just(it)
                }
        })
    }

    private fun <T> isContinuationClass(clazz: Class<T>) =
            Continuation::class.java.isAssignableFrom(clazz)
}

open class CompletableFutureContinuation(
) : Continuation<Any>, CompletableFuture<Any>() {
    override val context: CoroutineContext
        get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<Any>) {
        if (result.isFailure) {
            completeExceptionally(result.exceptionOrNull())
            return
        }
        val value = result.getOrNull()
        if (value is ReceiveChannel<*>) {
            val trueValue = value.asPublisher(Dispatchers.Unconfined)

            complete(trueValue)
        } else {
            complete(value)
        }
    }
}
