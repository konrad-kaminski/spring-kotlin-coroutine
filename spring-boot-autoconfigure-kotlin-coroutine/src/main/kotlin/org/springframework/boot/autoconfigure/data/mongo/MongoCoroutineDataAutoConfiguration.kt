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

import com.mongodb.reactivestreams.client.MongoClient
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.CoroutineMongoTemplate
import org.springframework.data.mongodb.core.ReactiveMongoOperations

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Data's coroutine mongo
 * support.
 * <p>
 * Registers a {@link CoroutineMongoTemplate} bean if no other bean of the same type is
 * configured.
 * <P>
 * Honors the {@literal spring.data.mongodb.database} property if set, otherwise connects
 * to the {@literal test} database.
 *
 * @author Konrad Kamiński
 * @author Mark Paluch
 */
@Configuration
@ConditionalOnClass(MongoClient::class, ReactiveMongoOperations::class)
@AutoConfigureAfter(MongoReactiveDataAutoConfiguration::class)
open class MongoCoroutineDataAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    open fun coroutineMongoTemplate(reactiveMongoOperations: ReactiveMongoOperations): CoroutineMongoTemplate =
            CoroutineMongoTemplate(reactiveMongoOperations)

    @Bean
    open fun mongoCoroutineBeanFactoryPostProcessor(): BeanFactoryPostProcessor = MongoCoroutineBeanFactoryPostProcessor()

    open class MongoCoroutineBeanFactoryPostProcessor : BeanFactoryPostProcessor {

        override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
            val registry = (beanFactory as BeanDefinitionRegistry) //TODO: fix this hack

            registry.beanDefinitionNames
                    .filter { it.startsWith("coroutine_") }
                    .map { it.substringAfter("coroutine_") }
                    .forEach {
                        registry.removeBeanDefinition(it)
                        registry.registerAlias("coroutine_$it", it)
                    }
        }
    }
}
