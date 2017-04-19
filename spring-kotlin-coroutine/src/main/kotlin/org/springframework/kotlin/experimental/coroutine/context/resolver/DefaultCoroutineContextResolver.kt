package org.springframework.kotlin.experimental.coroutine.context.resolver

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Unconfined
import org.springframework.kotlin.experimental.coroutine.context.CoroutineContextResolver
import kotlin.coroutines.experimental.CoroutineContext

internal open class DefaultCoroutineContextResolver : CoroutineContextResolver {
    override fun resolveContext(beanName: String, bean: Any?): CoroutineContext? =
            when (beanName) {
                "CommonPool" -> CommonPool
                "Unconfined" -> Unconfined
                else         -> bean as? CoroutineContext
            }
}