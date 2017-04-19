package org.springframework.kotlin.experimental.coroutine.context

import kotlin.coroutines.experimental.CoroutineContext

interface CoroutineContextResolver {
    fun resolveContext(beanName: String, bean: Any?): CoroutineContext?
}
