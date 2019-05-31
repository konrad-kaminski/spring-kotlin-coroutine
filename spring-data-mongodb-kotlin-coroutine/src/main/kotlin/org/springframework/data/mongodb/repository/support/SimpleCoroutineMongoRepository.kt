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

package org.springframework.data.mongodb.repository.support

import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.mongodb.core.CoroutineMongoOperations
import org.springframework.data.mongodb.core.CoroutineMongoTemplate
import org.springframework.data.mongodb.repository.CoroutineMongoRepository
import org.springframework.data.mongodb.repository.query.MongoEntityInformation
import java.io.Serializable

open class SimpleCoroutineMongoRepository<T, ID: Serializable>(
    entityInformation: MongoEntityInformation<T, ID>,
    mongoOperations: CoroutineMongoOperations
): CoroutineMongoRepository<T, ID> {
    private val reactiveRepo = SimpleReactiveMongoRepository<T, ID>(entityInformation, (mongoOperations as CoroutineMongoTemplate).reactiveMongoOperations)

    override suspend fun <S : T> save(entity: S): S =
        reactiveRepo.save(entity).awaitSingle()

    override suspend fun <S : T> saveAll(entities: Iterable<S>): List<S> =
        reactiveRepo.saveAll(entities).collectList().awaitSingle()

    override suspend fun findById(id: ID): T? =
        reactiveRepo.findById(id).awaitFirstOrDefault(null)

    override suspend fun existsById(id: ID): Boolean =
        reactiveRepo.existsById(id).awaitSingle()

    override suspend fun findAll(): List<T> =
        reactiveRepo.findAll().collectList().awaitSingle()

    override suspend fun findAllById(ids: Iterable<ID>): List<T> =
        reactiveRepo.findAllById(ids).collectList().awaitSingle()

    override suspend fun count(): Long =
        reactiveRepo.count().awaitSingle()

    override suspend fun deleteById(id: ID) {
        reactiveRepo.deleteById(id).awaitFirstOrDefault(null)
    }

    override suspend fun delete(entity: T) {
        reactiveRepo.delete(entity).awaitFirstOrDefault(null)
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        reactiveRepo.deleteAll(entities).awaitFirstOrDefault(null)
    }

    override suspend fun <S : T> insert(entity: S): S? =
        reactiveRepo.insert(entity).awaitFirstOrDefault(null)

    override suspend fun deleteAll() {
        reactiveRepo.deleteAll().awaitFirstOrDefault(null)
    }
}
