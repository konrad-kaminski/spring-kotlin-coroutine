package org.springframework.kotlin.experimental.coroutine

import org.springframework.context.annotation.AdviceMode
import org.springframework.context.annotation.Import
import org.springframework.core.Ordered
import org.springframework.kotlin.experimental.coroutine.cache.CoroutineCacheConfiguration
import org.springframework.kotlin.experimental.coroutine.context.CoroutineConfigurationSelector
import org.springframework.kotlin.experimental.coroutine.context.CoroutineContexts
import org.springframework.kotlin.experimental.coroutine.event.CoroutineEventSupportConfiguraton
import org.springframework.kotlin.experimental.coroutine.web.CoroutinesWebMvcConfigurerAdapter

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(CoroutineConfigurationSelector::class, CoroutinesWebMvcConfigurerAdapter::class,
        CoroutineContexts::class, CoroutineEventSupportConfiguraton::class,
        CoroutineCacheConfiguration::class)
annotation class EnableCoroutine(
        val proxyTargetClass: Boolean = false,
        val mode: AdviceMode = AdviceMode.PROXY,
        val order: Int = Ordered.LOWEST_PRECEDENCE
)
