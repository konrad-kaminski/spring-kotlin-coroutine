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

import kotlinx.coroutines.experimental.CoroutineName
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.kotlin.experimental.coroutine.annotation.Coroutine
import org.springframework.kotlin.experimental.coroutine.util.CoroutineUtils
import java.lang.reflect.Method
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext

private typealias ContextKey = Pair<String, String>

internal open class CoroutineMethodInterceptor(
        private val contextResolver: GlobalCoroutineContextResolver
) : MethodInterceptor {
    private val contextMap = ConcurrentHashMap<ContextKey, Optional<CoroutineContext>>().apply {
        put("" to "", Optional.empty())
    }

    override fun invoke(invocation: MethodInvocation): Any? =
            getMergedMethodOrClassAnnotation(invocation.method, Coroutine::class.java).let { coroutine ->
                val contextKey = coroutine.context to coroutine.name
                val context = contextMap[contextKey] ?: getContext(contextKey).apply { contextMap[contextKey] = this }

                if (context.isPresent) {
                    val originalContinuation = invocation.arguments.last() as Continuation<*>

                    CoroutineUtils.runCoroutine(context.get(), { _, cont ->
                        invocation.arguments[invocation.arguments.lastIndex] = cont
                        invocation.proceed()
                    }, originalContinuation)
                } else {
                    invocation.proceed()
                }
            }

    private fun getContext(contextKey: ContextKey): Optional<CoroutineContext> {
        val (contextBeanName, coroutineName) = contextKey

        val context = contextResolver.resolveContext(contextBeanName)

        val name = if (coroutineName != "") {
            CoroutineName(coroutineName)
        } else {
            null
        }

        return when {
            context != null && name != null -> context + name
            context != null                 -> context
            name != null                    -> name
            else                            -> null
        }.asOptional()
    }
}

private fun <T> T?.asOptional(): Optional<T> = Optional.ofNullable(this)

private fun <A: Annotation> getMergedMethodOrClassAnnotation(method: Method, annotationType: Class<A>): A =
        AnnotatedElementUtils.findMergedAnnotation(method, annotationType) ?:
                AnnotatedElementUtils.findMergedAnnotation(method.declaringClass, annotationType) ?:
                throw CannotFindAnnotation("Annotation $annotationType cannot be found on method $method")


open class CannotFindAnnotation(msg: String): RuntimeException(msg)

open class CannotObtainContextException(msg: String) : RuntimeException(msg)