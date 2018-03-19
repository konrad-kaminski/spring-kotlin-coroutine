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

package org.springframework.kotlin.experimental.coroutine.proxy.provider

import org.springframework.kotlin.experimental.coroutine.isSuspend
import org.springframework.kotlin.experimental.coroutine.proxy.CoroutineProxyConfig
import org.springframework.kotlin.experimental.coroutine.proxy.DefaultCoroutineProxyConfig
import org.springframework.kotlin.experimental.coroutine.proxy.MethodInvoker
import org.springframework.kotlin.experimental.coroutine.proxy.MethodInvokerProvider
import org.springframework.kotlin.experimental.coroutine.removeLastValue
import org.springframework.kotlin.experimental.coroutine.util.CoroutineUtils.runCoroutine
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.coroutines.experimental.Continuation
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction

@Suppress("UNCHECKED_CAST")
object CoroutineFromRegularMethodInvokerProvider: MethodInvokerProvider {

    override fun <T> createMethodInvoker(method: Method, coroutineInterface: Class<T>, obj: Any,
                                         proxyConfig: CoroutineProxyConfig): MethodInvoker? =

        if (method.isSuspend &&
            method.kotlinFunction!!.returnType.javaType == method.returnType &&
            proxyConfig is DefaultCoroutineProxyConfig) {
            try {
                obj.javaClass.getMethod(method.name, *method.parameterTypes.removeLastValue())
            } catch (e: NoSuchMethodException) {
                null
            }?.let { regularMethod ->

                if (proxyConfig.coroutineContext == null) {
                    object : MethodInvoker {
                        override fun invoke(vararg args: Any): Any? =
                            try {
                                regularMethod.invoke(obj, *args.removeLastValue())
                            } catch (ex: InvocationTargetException) {
                                throw ex.targetException
                            }
                    }
                } else {
                    object : MethodInvoker {
                        override fun invoke(vararg args: Any): Any? =
                            runCoroutine(proxyConfig.coroutineContext, {
                                    try {
                                        regularMethod.invoke(obj, *args.removeLastValue())
                                    } catch (ex: InvocationTargetException) {
                                        throw ex.targetException
                                    }
                                },
                                args.last() as Continuation<Any?>
                            )
                    }
                }
            }
        } else {
            null
        }
}