package org.springframework.kotlin.experimental.coroutine.context

import org.springframework.aop.framework.autoproxy.AbstractBeanFactoryAwareAdvisingPostProcessor
import org.springframework.beans.factory.BeanFactory

internal open class CoroutineAnnotationBeanPostProcessor(
        private val resolvers: Set<CoroutineContextResolver>
) : AbstractBeanFactoryAwareAdvisingPostProcessor() {
    override fun setBeanFactory(beanFactory: BeanFactory) {
        advisor = CoroutinePointcutAdvisor(beanFactory, resolvers)
    }
}
