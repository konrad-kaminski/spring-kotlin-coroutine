package org.springframework.kotlin.experimental.coroutine.event

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Role
import org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
internal open class CoroutineEventSupportConfiguraton {
    @Bean
    open fun coroutineEventListenerFactory(applicationContext: ApplicationContext,
        eventPublisher: CoroutineApplicationEventPublisher) =
            CoroutineEventListenerFactory(applicationContext, eventPublisher)

    @Bean
    open fun coroutineApplicationEventPublisherAwareBeanPostProcessor(
            publisher: CoroutineApplicationEventPublisher,
            applicationContext: ConfigurableApplicationContext
    ) = CoroutineApplicationEventPublisherAwareBeanPostProcessor(applicationContext, publisher)

    @Bean
    open fun coroutineApplicationEventPublisher(
            publisher: ApplicationEventPublisher
    ) = DelegatingCoroutineApplicationEventPublisher(publisher)

    @Bean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
    open fun coroutineApplicationEventMulticaster() = CoroutineApplicationEventMulticaster()
}