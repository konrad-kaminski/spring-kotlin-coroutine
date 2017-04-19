package org.springframework.kotlin.experimental.coroutine.event

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ConfigurableApplicationContext
import java.security.AccessController
import java.security.PrivilegedAction

internal open class CoroutineApplicationEventPublisherAwareBeanPostProcessor(
        private val applicationContext: ConfigurableApplicationContext,
        private val publisher: CoroutineApplicationEventPublisher
) : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any = bean.apply {
        if (this is CoroutineApplicationEventPublisherAware) {
            val acc = applicationContext.beanFactory.accessControlContext

            if (acc != null) {
                AccessController.doPrivileged(PrivilegedAction {
                    setCoroutineApplicationEventPublisher(publisher)
                }, acc)
            } else {
                setCoroutineApplicationEventPublisher(publisher)
            }
        }
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = bean
}
