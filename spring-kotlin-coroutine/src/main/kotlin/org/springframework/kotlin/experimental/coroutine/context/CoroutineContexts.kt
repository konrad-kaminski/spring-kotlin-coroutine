package org.springframework.kotlin.experimental.coroutine.context

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Unconfined
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Role

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
internal open class CoroutineContexts {
    @Bean(COMMON_POOL)
    open fun commonPool() = CommonPool

    @Bean(UNCONFINED)
    open fun unconfined() = Unconfined
}

const val COMMON_POOL = "CommonPool"
const val UNCONFINED = "Unconfined"
