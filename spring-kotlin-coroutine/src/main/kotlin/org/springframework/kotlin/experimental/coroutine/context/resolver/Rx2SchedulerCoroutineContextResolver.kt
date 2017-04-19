package org.springframework.kotlin.experimental.coroutine.context.resolver

import io.reactivex.Scheduler
import kotlinx.coroutines.experimental.rx2.asCoroutineDispatcher
import org.springframework.kotlin.experimental.coroutine.context.CoroutineContextResolver
import kotlin.coroutines.experimental.CoroutineContext

internal open class Rx2SchedulerCoroutineContextResolver : CoroutineContextResolver {
    override fun resolveContext(beanName: String, bean: Any?): CoroutineContext? =
            (bean as? Scheduler)?.asCoroutineDispatcher()
}