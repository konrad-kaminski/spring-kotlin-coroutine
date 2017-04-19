package org.springframework.context.event

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.kotlin.experimental.coroutine.event.CoroutineApplicationEventPublisher
import java.lang.reflect.Method

private val evaluator = EventExpressionEvaluator()

internal fun createApplicationListener(applicationContext: ApplicationContext,
    beanName: String, type: Class<*>, method: Method, publisher: CoroutineApplicationEventPublisher
): ApplicationListener<*> =
        SimpleCoroutineApplicationListenerMethodAdapter(beanName, type, method, publisher).apply {
            init(applicationContext, evaluator)
        }