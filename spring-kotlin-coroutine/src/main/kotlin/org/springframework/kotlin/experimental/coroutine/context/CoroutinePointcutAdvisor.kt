package org.springframework.kotlin.experimental.coroutine.context

import org.aopalliance.aop.Advice
import org.springframework.aop.Pointcut
import org.springframework.aop.support.AbstractPointcutAdvisor
import org.springframework.aop.support.ComposablePointcut
import org.springframework.aop.support.StaticMethodMatcher
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut
import org.springframework.beans.factory.BeanFactory
import org.springframework.kotlin.experimental.coroutine.annotation.Coroutine
import org.springframework.kotlin.experimental.coroutine.isSuspend
import java.lang.reflect.Method

internal open class CoroutinePointcutAdvisor(
        beanFactory: BeanFactory,
        resolvers: Set<CoroutineContextResolver>
) : AbstractPointcutAdvisor() {
    private val advice: Advice
    private val pointcut: Pointcut

    init {
        val classMatchingPointcut = AnnotationMatchingPointcut(Coroutine::class.java, true)
        val methodMatchingPointcut = AnnotationMatchingPointcut.forMethodAnnotation(Coroutine::class.java)
        pointcut = ComposablePointcut(classMatchingPointcut).union(methodMatchingPointcut).intersection(CoroutineMethodMatcher)

        advice = CoroutineMethodInterceptor(beanFactory, resolvers)
    }

    override fun getAdvice(): Advice = advice

    override fun getPointcut(): Pointcut = pointcut
}

private object CoroutineMethodMatcher: StaticMethodMatcher() {
    override fun matches(method: Method, targetClass: Class<*>) = method.isSuspend
}
