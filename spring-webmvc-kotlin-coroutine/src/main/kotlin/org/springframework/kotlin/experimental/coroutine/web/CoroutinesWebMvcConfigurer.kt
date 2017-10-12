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
import org.springframework.web.context.request.async.WebAsyncUtils
import org.springframework.web.method.support.AsyncHandlerMethodReturnValueHandler
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlin.coroutines.experimental.intrinsics.COROUTINE_SUSPENDED

@Configuration
@ConditionalOnClass(WebMvcConfigurer::class)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
internal open class CoroutinesWebMvcConfigurer : WebMvcConfigurer {
    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers.add(0, object: HandlerMethodArgumentResolver {
            override fun supportsParameter(parameter: MethodParameter) =
                    parameter.method.isSuspend && isContinuationClass(parameter.parameterType)

            override fun resolveArgument(parameter: MethodParameter, mavContainer: ModelAndViewContainer,
                                         webRequest: NativeWebRequest, binderFactory: WebDataBinderFactory) =
                    DeferredResultContinuation().apply {
                        mavContainer.model[CONTINUATION] = this
                    }
        })
    }

    override fun addReturnValueHandlers(returnValueHandlers: MutableList<HandlerMethodReturnValueHandler>) {
        returnValueHandlers.add(0, object: AsyncHandlerMethodReturnValueHandler {
            override fun supportsReturnType(returnType: MethodParameter): Boolean =
                    returnType.method.isSuspend

            override fun handleReturnValue(returnValue: Any?, returnType: MethodParameter,
                                           mavContainer: ModelAndViewContainer, webRequest: NativeWebRequest) =
                if (returnValue == COROUTINE_SUSPENDED) {
                    val result = (mavContainer.model[CONTINUATION] as DeferredResultContinuation).deferredResult

                    WebAsyncUtils.getAsyncManager(webRequest).startDeferredResultProcessing(result, mavContainer)
                } else {
                    mavContainer.isRequestHandled = true
                }

            override fun isAsyncReturnValue(returnValue: Any, returnType: MethodParameter): Boolean =
                    returnValue == COROUTINE_SUSPENDED
        })
    }

    private fun <T> isContinuationClass(clazz: Class<T>) =
            Continuation::class.java.isAssignableFrom(clazz)

    companion object {
        private const val CONTINUATION = "continuation"
    }
}

private class DeferredResultContinuation: Continuation<Any> {
    val deferredResult = DeferredResult<Any>()

    override val context: CoroutineContext
        get() = EmptyCoroutineContext

    override fun resume(value: Any) {
        deferredResult.setResult(value)
    }

    override fun resumeWithException(exception: Throwable) {
        deferredResult.setErrorResult(exception)
    }
}