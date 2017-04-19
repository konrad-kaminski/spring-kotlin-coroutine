package org.springframework.kotlin.experimental.coroutine.context.resolver

import kotlinx.coroutines.experimental.asCoroutineDispatcher
import org.springframework.kotlin.experimental.coroutine.context.CoroutineContextResolver
import java.util.concurrent.Executor
import kotlin.coroutines.experimental.CoroutineContext

internal open class ExecutorCoroutineContextResolver : CoroutineContextResolver {
    override fun resolveContext(beanName: String, bean: Any?): CoroutineContext? =
            (bean as? Executor)?.asCoroutineDispatcher()
}