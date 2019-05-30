/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.kotlin.coroutine.scheduler

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.ImportAware
import org.springframework.context.annotation.Role
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata
import org.springframework.kotlin.coroutine.EnableCoroutine
import org.springframework.kotlin.coroutine.context.GlobalCoroutineContextResolver
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor
import org.springframework.scheduling.config.TaskManagementConfigUtils

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Import(CoroutineSchedulerSupportingConfiguration::class)
internal open class CoroutineSchedulerConfiguration: ImportAware {
    private lateinit var schedulerDispatcher: String

    override fun setImportMetadata(importMetadata: AnnotationMetadata) {
        schedulerDispatcher = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableCoroutine::class.java.name, false))["schedulerDispatcher"].toString()
    }

    @Bean(COROUTINE_SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
    open fun coroutineScheduledAnnotationBeanPostProcessor(
            contextResolver: GlobalCoroutineContextResolver
    ): ScheduledAnnotationBeanPostProcessor =
        CoroutineScheduledAnnotationBeanPostProcessor(schedulerDispatcher, contextResolver, DefaultSchedulingPolicyProvider())
}

@Configuration
@Role(BeanDefinition.ROLE_SUPPORT)
internal open class CoroutineSchedulerSupportingConfiguration {
    @Bean
    open fun coroutineSchedulerPostProcessor(): BeanDefinitionRegistryPostProcessor = CoroutineSchedulerPostProcessor()
}

private const val COROUTINE_SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME = "coroutineScheduledAnnotationProcessor"

private class CoroutineSchedulerPostProcessor : BeanDefinitionRegistryPostProcessor {
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {}

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        if (registry.containsBeanDefinition(TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
            registry.removeBeanDefinition(TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
            registry.registerAlias(COROUTINE_SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME, TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
        } else {
            registry.removeBeanDefinition(COROUTINE_SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
        }
    }
}