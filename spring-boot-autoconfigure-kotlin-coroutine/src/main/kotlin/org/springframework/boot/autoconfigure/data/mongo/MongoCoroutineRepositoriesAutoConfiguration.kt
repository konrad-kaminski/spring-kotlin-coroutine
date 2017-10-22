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
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.repository.CoroutineMongoRepository
import org.springframework.data.mongodb.repository.config.CoroutineMongoRepositoryConfigurationExtension
import org.springframework.data.mongodb.repository.config.EnableCoroutineMongoRepositories
import org.springframework.data.mongodb.repository.support.CoroutineMongoRepositoryFactoryBean

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Data's Mongo coroutine
 * Repositories.
 * <p>
 * Activates when there is no bean of type
 * {@link org.springframework.data.mongodb.repository.support.CoroutineMongoRepositoryFactoryBean}
 * configured in the context, the Spring Data Mongo {@link CoroutineMongoRepository} type
 * is on the classpath and there is no other configured {@link CoroutineMongoRepository}.
 * <p>
 * Once in effect, the auto-configuration is the equivalent of enabling Mongo repositories
 * using the {@link EnableCoroutineMongoRepositories} annotation.
 *
 * @author Konrad Kamiński
 * @author Mark Paluch
 * @see EnableCoroutineMongoRepositories
 */
@Configuration
@ConditionalOnClass(MongoClient::class, CoroutineMongoRepository::class)
@ConditionalOnMissingBean(CoroutineMongoRepositoryFactoryBean::class, CoroutineMongoRepositoryConfigurationExtension::class)
@ConditionalOnProperty(prefix = "spring.data.mongodb.coroutine-repositories", name = arrayOf("enabled"), havingValue = "true", matchIfMissing = true)
@Import(MongoCoroutineRepositoriesAutoConfigureRegistrar::class)
@AutoConfigureAfter(MongoCoroutineDataAutoConfiguration::class)
open class MongoCoroutineRepositoriesAutoConfiguration
