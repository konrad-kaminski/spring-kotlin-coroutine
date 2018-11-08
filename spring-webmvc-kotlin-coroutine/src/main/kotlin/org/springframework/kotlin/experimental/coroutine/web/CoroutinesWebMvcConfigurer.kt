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

package org.springframework.kotlin.experimental.coroutine.web

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Role
import org.springframework.core.MethodParameter
import org.springframework.kotlin.experimental.coroutine.ConditionalOnClass
import org.springframework.kotlin.experimental.coroutine.isSuspend
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.async.DeferredResult
import org.springframework.web.method.support.AsyncHandlerMethodReturnValueHandler
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.mvc.method.annotation.DeferredResultMethodReturnValueHandler
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

@Configuration
@ConditionalOnClass("org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
internal open class CoroutinesWebMvcConfigurer : WebMvcConfigurer {
    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers.add(0, object: HandlerMethodArgumentResolver {
            override fun supportsParameter(parameter: MethodParameter) =
                    parameter.method.isSuspend && isContinuationClass(parameter.parameterType)

            override fun resolveArgument(parameter: MethodParameter, mavContainer: ModelAndViewContainer,
                                         webRequest: NativeWebRequest, binderFactory: WebDataBinderFactory) =
                    object: Continuation<Any> {
                        val deferredResult = DeferredResult<Any>()

                        override val context: CoroutineContext
                        get() = EmptyCoroutineContext

                        override fun resume(value: Any) {
                            deferredResult.setResult(value)
                        }

                        override fun resumeWithException(exception: Throwable) {
                            deferredResult.setErrorResult(exception)
                        }
                    }.apply {
                        mavContainer.model[DEFERRED_RESULT] = deferredResult
                    }
        })
    }

    override fun addReturnValueHandlers(returnValueHandlers: MutableList<HandlerMethodReturnValueHandler>) {
        returnValueHandlers.add(0, object: AsyncHandlerMethodReturnValueHandler {
            private val delegate = DeferredResultMethodReturnValueHandler()

            override fun supportsReturnType(returnType: MethodParameter): Boolean =
                    returnType.method.isSuspend

            override fun handleReturnValue(returnValue: Any?, type: MethodParameter,
                                           mavContainer: ModelAndViewContainer, webRequest: NativeWebRequest) {
                val result = mavContainer.model[DEFERRED_RESULT] as DeferredResult<*>

                return delegate.handleReturnValue(result, type, mavContainer, webRequest)
            }

            override fun isAsyncReturnValue(returnValue: Any, returnType: MethodParameter): Boolean =
                    returnValue === COROUTINE_SUSPENDED
        })
    }

    private fun <T> isContinuationClass(clazz: Class<T>) = Continuation::class.java.isAssignableFrom(clazz)

    companion object {
        private const val DEFERRED_RESULT = "deferred_result"
    }
}
