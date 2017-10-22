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

package org.springframework.kotlin.experimental.coroutine.cache

import org.aopalliance.intercept.MethodInvocation
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.interceptor.CacheInterceptor
import org.springframework.cache.interceptor.CacheOperationInvoker
import org.springframework.cache.interceptor.CacheOperationSource
import org.springframework.cache.interceptor.SimpleKeyGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Role
import org.springframework.kotlin.experimental.coroutine.isSuspend
import org.springframework.kotlin.experimental.coroutine.removeLastValue
import org.springframework.kotlin.experimental.coroutine.setLast
import java.lang.reflect.Method
import java.util.Optional
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.intrinsics.COROUTINE_SUSPENDED

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
internal open class CoroutineCacheConfiguration {
    @Bean
    open fun cachingPostProcessor(): BeanDefinitionRegistryPostProcessor = CoroutineCacheBeanFactoryPostProcessor()

    @Bean(COROUTINE_CACHE_INTERCEPTOR_BEAN)
    open fun coroutineCacheInterceptor(config: Optional<CachingConfigurer>, cacheOperationSource: CacheOperationSource): CacheInterceptor =
            CoroutineCacheInterceptor().apply {
                setCacheOperationSources(cacheOperationSource)
                config.ifPresent {
                    it.cacheManager()?.let { setCacheManager(it) }
                    it.cacheResolver()?.let { cacheResolver = it }
                    it.keyGenerator()?.let { keyGenerator = it }
                    it.errorHandler()?.let { errorHandler = it }
                }


                if (keyGenerator is SimpleKeyGenerator) {
                    keyGenerator = CoroutineAwareSimpleKeyGenerator()
                }
            }
}

private class CoroutineCacheBeanFactoryPostProcessor : BeanDefinitionRegistryPostProcessor {
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {}

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        if (registry.containsBeanDefinition(CACHE_INTERCEPTOR_BEAN)) {
            registry.removeBeanDefinition(CACHE_INTERCEPTOR_BEAN)
            registry.registerAlias(COROUTINE_CACHE_INTERCEPTOR_BEAN, CACHE_INTERCEPTOR_BEAN)
        } else {
            registry.removeBeanDefinition(COROUTINE_CACHE_INTERCEPTOR_BEAN)
        }
    }
}

private const val CACHE_INTERCEPTOR_BEAN = "cacheInterceptor"
private const val COROUTINE_CACHE_INTERCEPTOR_BEAN = "coroutineCacheInterceptor"

private class CoroutineCacheInterceptor: CacheInterceptor() {
    override fun invoke(invocation: MethodInvocation): Any? =
            if (invocation.method.isSuspend) {
                invokeCoroutine(invocation)
            }
            else {
                super.invoke(invocation)
            }

    @Suppress("UNCHECKED_CAST")
    private fun invokeCoroutine(invocation: MethodInvocation): Any? =
            try {
                val args = invocation.arguments
                val target = invocation.`this`
                val method = invocation.method

                val cachingContinuation = CachingContinuation(args.last() as Continuation<Any?>) {
                    execute({ it }, target, method, args)
                }

                executeCoroutine(cachingContinuation, invocation, target, method, args)
            } catch (th: CacheOperationInvoker.ThrowableWrapper) {
                throw th.original
            }

    private fun executeCoroutine(cachingContinuation: CachingContinuation,
                                 invocation: MethodInvocation, target: Any, method: Method, args: Array<Any>): Any? {

        val originalContinuation = args.last()
        args.setLast(cachingContinuation)

        return try {
            execute({
                try {
                    invocation.proceed()
                } catch (ex: Throwable) {
                    throw CacheOperationInvoker.ThrowableWrapper(ex)
                }
            }, target, method, args)
        } catch (e: CoroutineSuspendedException) {
            COROUTINE_SUSPENDED
        } finally {
            args.setLast(originalContinuation)
        }
    }

    override fun invokeOperation(invoker: CacheOperationInvoker): Any? {
        val result = super.invokeOperation(invoker)

        if (result === COROUTINE_SUSPENDED) {
            throw CoroutineSuspendedException()
        } else {
            return result
        }
    }
}

private class CachingContinuation(
        private val delegate: Continuation<Any?>,
        private val onResume: (Any?) -> Unit
): Continuation<Any?> {
    override val context = delegate.context

    override fun resume(value: Any?) {
        var resumed = false

        try {
            onResume(value)
        }
        catch (ex: Throwable) {
            resumed = true
            delegate.resumeWithException(ex)
        }

        if (!resumed) {
            delegate.resume(value)
        }
    }

    override fun resumeWithException(exception: Throwable) =
            delegate.resumeWithException(exception)
}

private class CoroutineSuspendedException: RuntimeException()

private class CoroutineAwareSimpleKeyGenerator: SimpleKeyGenerator() {
    override fun generate(target: Any, method: Method, vararg params: Any): Any =
            when {
                method.isSuspend -> super.generate(target, method, *params.removeLastValue())
                else             -> super.generate(target, method, *params)
            }
}
