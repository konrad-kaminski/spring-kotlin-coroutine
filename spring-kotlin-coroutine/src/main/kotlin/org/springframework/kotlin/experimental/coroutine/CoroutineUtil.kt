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

package org.springframework.kotlin.experimental.coroutine

import org.springframework.kotlin.experimental.coroutine.util.CoroutineUtils.runCoroutine
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.reflect.jvm.kotlinFunction

val Method.isSuspend: Boolean
    get() = kotlinFunction?.isSuspend ?: false

@Suppress("UNCHECKED_CAST")
fun <T: Any> createCoroutineProxy(coroutineInterface: Class<T>, obj: Any, context: CoroutineContext? = null): T =
        Proxy.newProxyInstance(coroutineInterface.classLoader, arrayOf(coroutineInterface)) { _, method, args ->
            if (method.isSuspend) {
                if (context == null) {
                    invokeRegularMethod(obj, method, args)
                } else {
                    runCoroutine(context, { invokeRegularMethod(obj, method, args) },
                            args.last() as Continuation<Any?>)
                }
            } else {
                method.invoke(args)
            }
        } as T

private fun invokeRegularMethod(obj: Any, method: Method, args: Array<Any>): Any? {
    val regularMethod = obj.javaClass.getMethod(method.name, *method.parameterTypes.removeLastValue())

    return regularMethod.invoke(obj, *args.removeLastValue())
}

@Suppress("UNCHECKED_CAST")
internal fun <T> Array<T>.removeLastValue(): Array<T> =
        copyOf(size - 1) as Array<T>

internal fun <T> Array<T>.setLast(value: T): Unit {
    this[lastIndex] = value
}