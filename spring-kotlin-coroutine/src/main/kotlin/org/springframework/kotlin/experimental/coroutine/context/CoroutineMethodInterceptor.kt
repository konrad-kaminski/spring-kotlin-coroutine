package org.springframework.kotlin.experimental.coroutine.context

import kotlinx.coroutines.experimental.CoroutineName
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.NoSuchBeanDefinitionException
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
        private val beanFactory: BeanFactory,
        private val resolvers: Set<CoroutineContextResolver>
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

                    CoroutineUtils.runCoroutine(context.get(), { cont ->
                        invocation.arguments[invocation.arguments.lastIndex] = cont
                        invocation.proceed()
                    }, originalContinuation)
                } else {
                    invocation.proceed()
                }
            }

    private fun getContext(contextKey: ContextKey): Optional<CoroutineContext> {
        val (contextBeanName, coroutineName) = contextKey

        val context = if (contextBeanName != "") {
            resolvers.asSequence().mapNotNull {
                it.resolveContext(contextBeanName, beanFactory.findBean(contextBeanName))
            }.firstOrNull() ?: throw CannotObtainContextException("Cannot obtain context $contextBeanName")
        } else {
            null
        }

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

private fun BeanFactory.findBean(name: String): Any? =
        try {
            getBean(name)
        } catch (e: NoSuchBeanDefinitionException) {
            //ignore
            null
        }

private fun <A: Annotation> getMergedMethodOrClassAnnotation(method: Method, annotationType: Class<A>): A =
        AnnotatedElementUtils.findMergedAnnotation(method, annotationType) ?:
                AnnotatedElementUtils.findMergedAnnotation(method.declaringClass, annotationType) ?:
                throw CannotFindAnnotation("Annotation $annotationType cannot be found on method $method")


open class CannotFindAnnotation(msg: String): RuntimeException(msg)

open class CannotObtainContextException(msg: String) : RuntimeException(msg)