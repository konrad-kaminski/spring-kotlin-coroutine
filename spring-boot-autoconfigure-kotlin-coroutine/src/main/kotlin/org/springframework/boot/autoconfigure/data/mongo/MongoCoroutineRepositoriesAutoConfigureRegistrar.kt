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

package org.springframework.boot.autoconfigure.data.mongo

import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport
import org.springframework.core.env.Environment
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.StandardAnnotationMetadata
import org.springframework.data.mongodb.repository.config.CoroutineMongoRepositoryConfigurationExtension
import org.springframework.data.mongodb.repository.config.EnableCoroutineMongoRepositories
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource
import org.springframework.data.repository.config.RepositoryConfigurationDelegate
import org.springframework.data.repository.config.RepositoryConfigurationExtension
import org.springframework.data.util.Streamable

/**
 * {@link ImportBeanDefinitionRegistrar} used to auto-configure Spring Data Mongo Coroutine
 * Repositories.
 *
 * @author Konrad Kamiński
 */
open class MongoCoroutineRepositoriesAutoConfigureRegistrar: AbstractRepositoryConfigurationSourceSupport() {

    private lateinit var resourceLoader: ResourceLoader
    private lateinit var environment: Environment
    private lateinit var beanFactory: BeanFactory

    override fun getAnnotation(): Class<out Annotation> =
        EnableCoroutineMongoRepositories::class.java

    override fun getConfiguration(): Class<*> =
        EnableCoroutineMongoRepositoriesConfiguration::class.java

    override fun getRepositoryConfigurationExtension(): RepositoryConfigurationExtension =
        CoroutineMongoRepositoryConfigurationExtension()

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata,
                                         registry: BeanDefinitionRegistry) {
        RepositoryConfigurationDelegate(getConfigurationSource(registry),
                this.resourceLoader, this.environment).registerRepositoriesIn(registry,
                repositoryConfigurationExtension)
    }

    private fun getConfigurationSource(
            beanDefinitionRegistry: BeanDefinitionRegistry): AnnotationRepositoryConfigurationSource {
        val metadata = StandardAnnotationMetadata(configuration, true)

        return object : AnnotationRepositoryConfigurationSource(metadata, annotation,
                resourceLoader, environment, beanDefinitionRegistry) {
            override fun generateBeanName(beanDefinition: BeanDefinition): String =
                "coroutine_" + super.generateBeanName(beanDefinition)

            override fun getBasePackages(): Streamable<String> =
                this@MongoCoroutineRepositoriesAutoConfigureRegistrar.basePackages
        }
    }

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
        super.setResourceLoader(resourceLoader)
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
        super.setBeanFactory(beanFactory)
    }

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
        super.setEnvironment(environment)
    }


    @EnableCoroutineMongoRepositories
    private class EnableCoroutineMongoRepositoriesConfiguration
}