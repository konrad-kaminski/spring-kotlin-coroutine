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

package org.springframework.kotlin.coroutine.proxy.provider

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.springframework.kotlin.coroutine.proxy.CoroutineProxyConfig
import org.springframework.kotlin.coroutine.proxy.DeferredCoroutineProxyConfig
import org.springframework.kotlin.coroutine.proxy.MethodInvoker
import org.springframework.kotlin.coroutine.proxy.MethodInvokerProvider
import java.lang.reflect.Method

object DeferredFromRegularMethodInvokerProvider : MethodInvokerProvider {

    override fun <T> createMethodInvoker(method: Method, coroutineInterface: Class<T>, obj: Any,
                                         proxyConfig: CoroutineProxyConfig): MethodInvoker? =

        if (method.returnType == Deferred::class.java &&
            proxyConfig is DeferredCoroutineProxyConfig) {

            try {
                obj.javaClass.getMethod(method.name, *method.parameterTypes)
            } catch (e: NoSuchMethodException) {
                null
            }?.let { regularMethod ->
                createDeferredFromRegularMethodInvoker(proxyConfig, regularMethod, obj)
            }
        } else {
            null
        }

    private fun createDeferredFromRegularMethodInvoker(proxyConfig: DeferredCoroutineProxyConfig, regularMethod: Method, obj: Any) =
        object : MethodInvoker {
            override fun invoke(vararg args: Any): Any? = GlobalScope.async(proxyConfig.coroutineContext, proxyConfig.start) {
                regularMethod.smartInvoke(obj, *args)
            }
        }
}
