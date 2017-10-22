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

package org.springframework.data.mongodb.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.CoroutineMongoTemplate
import org.springframework.data.mongodb.core.ReactiveMongoOperations

@Configuration
abstract class AbstractCoroutineMongoConfiguration : MongoConfigurationSupport() {
    /**
     * Creates a [CoroutineMongoTemplate].
     *
     * @return
     */
    @Bean
    open fun coroutineMongoTemplate(reactiveMongoOperations: ReactiveMongoOperations): CoroutineMongoTemplate =
            CoroutineMongoTemplate(reactiveMongoOperations)
}


