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

package org.springframework.kotlin.coroutine.proxy

import org.springframework.kotlin.coroutine.proxy.provider.CoroutineFromRegularMethodInvokerProvider
import org.springframework.kotlin.coroutine.proxy.provider.DeferredFromRegularMethodInvokerProvider
import org.springframework.kotlin.coroutine.proxy.provider.SameMethodInvokerProvider
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
fun <T: Any> createCoroutineProxy(coroutineInterface: Class<T>, obj: Any, proxyConfig: CoroutineProxyConfig): T =
        Proxy.newProxyInstance(coroutineInterface.classLoader, arrayOf(coroutineInterface),
                CoroutineProxyInvocationHandler(DEFAULT_METHOD_INVOKER_PROVIDERS, coroutineInterface, obj, proxyConfig)) as T

private val DEFAULT_METHOD_INVOKER_PROVIDERS = sequenceOf(
        CoroutineFromRegularMethodInvokerProvider,
        DeferredFromRegularMethodInvokerProvider,
        SameMethodInvokerProvider)

private class CoroutineProxyInvocationHandler<T>(val methodInvokerProviders: Sequence<MethodInvokerProvider>,
                                                 val coroutineInterface: Class<T>, val obj: Any, val proxyConfig: CoroutineProxyConfig): InvocationHandler {

    private val invokers = ConcurrentHashMap<Method, MethodInvoker>()

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>): Any? =
        (invokers[method] ?: createInvoker(method).apply {
            invokers[method] = this
        }).invoke(*args)

    private fun createInvoker(method: Method): MethodInvoker =
        methodInvokerProviders.mapNotNull {
            it.createMethodInvoker(method, coroutineInterface, obj, proxyConfig)
        }.firstOrNull() ?: throw NoInvokerFoundException(method)
}

open class NoInvokerFoundException(method: Method): RuntimeException("No invoker found for method: ${method.name}")
