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

package org.springframework.web.reactive.result.method.annotation

import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.reactive.asPublisher
import org.reactivestreams.Publisher
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.ReactiveAdapterRegistry
import org.springframework.kotlin.experimental.coroutine.returnTypeMetadata
import org.springframework.util.ClassUtils
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.HandlerResult
import org.springframework.web.reactive.result.method.InvocableHandlerMethod
import org.springframework.web.reactive.result.method.SyncInvocableHandlerMethod
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.lang.reflect.Proxy
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.experimental.intrinsics.COROUTINE_SUSPENDED

private class CustomControllerMethodResolver(
        private val delegate: ControllerMethodResolver
) : ControllerMethodResolver(
        ArgumentResolverConfigurer(), emptyList(), ReactiveAdapterRegistry(), nullContext
) {
    override fun getRequestMappingMethod(handlerMethod: HandlerMethod): InvocableHandlerMethod? =
        delegate.getRequestMappingMethod(handlerMethod)?.let { requestMappingMethod ->
            CoroutineInvocableHandlerMethod(handlerMethod).apply {
                setArgumentResolvers(requestMappingMethod.resolvers)
            }
        }

    override fun getModelAttributeMethods(handlerMethod: HandlerMethod?): MutableList<InvocableHandlerMethod>? =
            delegate.getModelAttributeMethods(handlerMethod)

    override fun getSessionAttributesHandler(handlerMethod: HandlerMethod?): SessionAttributesHandler? =
            delegate.getSessionAttributesHandler(handlerMethod)

    override fun getInitBinderMethods(handlerMethod: HandlerMethod?): MutableList<SyncInvocableHandlerMethod>? =
            delegate.getInitBinderMethods(handlerMethod)

    override fun getExceptionHandlerMethod(ex: Throwable?, handlerMethod: HandlerMethod?): InvocableHandlerMethod? =
            delegate.getExceptionHandlerMethod(ex, handlerMethod)
}

private val nullContext: ConfigurableApplicationContext =
        Proxy.newProxyInstance(ConfigurableApplicationContext::class.java.classLoader, arrayOf(ConfigurableApplicationContext::class.java)) { _, method, _ ->
            if (method.name == "getBeanNamesForType") {
                emptyArray<String>()
            } else {
                null
            }
        } as ConfigurableApplicationContext

private class CoroutineInvocableHandlerMethod(private val handlerMethod: HandlerMethod): InvocableHandlerMethod(handlerMethod) {

    override fun invoke(exchange: ServerWebExchange, bindingContext: BindingContext, vararg providedArgs: Any?): Mono<HandlerResult> =
            super.invoke(exchange, bindingContext, *providedArgs)
                    .map { result ->
                        if (result.returnValue === COROUTINE_SUSPENDED) {
                            val future = (bindingContext.model.asMap()["__continuation"] as AtomicReference<CompletableFuture<*>>).get()
                            val metaData = handlerMethod.method.returnTypeMetadata
                            val value = Mono.fromFuture(future).let { mono ->
                                    if (ClassUtils.isAssignable(ReceiveChannel::class.java, metaData.clazz)) {
                                        mono.flatMapMany {
                                            when (it) {
                                                is Publisher<*> -> it
                                                else       -> Mono.just(it)
                                            }
                                        }
                                    } else {
                                        mono
                                    }
                                }

                            HandlerResult(this, value, returnType, bindingContext)
                        } else if (result.returnValue is ReceiveChannel<*>) {
                            val value = (result.returnValue as ReceiveChannel<*>).asPublisher(Unconfined)

                            HandlerResult(this, value, returnType, bindingContext)
                        } else {
                            result
                        }
                    }
}

internal fun injectCustomControllerMethodResolver(adapter: RequestMappingHandlerAdapter) {
    val methodResolverField = RequestMappingHandlerAdapter::class.java.getDeclaredField("methodResolver")
    methodResolverField.isAccessible = true
    val resolver = methodResolverField.get(adapter) as ControllerMethodResolver
    if (resolver !is CustomControllerMethodResolver) {
        val newResolver = CustomControllerMethodResolver(resolver)
        methodResolverField.set(adapter, newResolver)
    }
}