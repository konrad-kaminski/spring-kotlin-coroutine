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

package test

import kotlinx.coroutines.channels.ReceiveChannel
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.repository.CoroutineMongoRepository

@Document
open class TestEntity {
    @Id
    var id: String? = null

    @Field
    var name: String = "_UNDEFINED_"

    @Field
    var number: Int = 13
}

interface TestRepository: CoroutineMongoRepository<TestEntity, String> {
    suspend fun findByName(name: String): List<TestEntity>

    suspend fun findByNumber(number: Int): TestEntity?
}

interface TestRepository2: CoroutineMongoRepository<TestEntity, String> {
    suspend fun findByName(name: String): ReceiveChannel<TestEntity>
}

interface TestRepository3: CoroutineMongoRepository<TestEntity, String> {
    fun findByName(name: String): ReceiveChannel<TestEntity>

    fun findByNumber(number: Int): TestEntity?
}

interface TestRepository4: CoroutineMongoRepository<TestEntity, String> {
    fun findByName(name: String): List<TestEntity>
}

