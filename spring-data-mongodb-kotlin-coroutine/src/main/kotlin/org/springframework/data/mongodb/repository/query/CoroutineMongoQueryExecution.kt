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

package org.springframework.data.mongodb.repository.query

import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.EntityInstantiators
import org.springframework.data.domain.Pageable
import org.springframework.data.geo.GeoResult
import org.springframework.data.mongodb.core.CoroutineMongoOperations
import org.springframework.data.mongodb.core.query.NearQuery
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.repository.query.ResultProcessor
import org.springframework.data.repository.util.ReactiveWrappers
import org.springframework.data.util.TypeInformation
import org.springframework.kotlin.experimental.coroutine.TypeMetaData
import org.springframework.util.ClassUtils

interface CoroutineMongoQueryExecution {
    /**
     * [CoroutineMongoQueryExecution] for collection returning queries.
     *
     * @author Konrad Kamiński
     */
    open class CollectionExecution(
            private val operations: CoroutineMongoOperations,
            private val pageable: Pageable,
            private val returnTypeMetaData: TypeMetaData
    ) : CoroutineMongoQueryExecution {
        override suspend fun execute(query: Query, type: Class<*>, collection: String): Any =
            operations.find(query.with(pageable), type, collection).let {
                if (returnTypeMetaData.clazz == List::class.java) {
                    it.toList()
                } else {
                    it
                }
            }
    }

    suspend fun <T> ReceiveChannel<T>.toList(): List<T> = mutableListOf<T>().apply { consumeEach { add(it) }}

    /**
     * [CoroutineMongoQueryExecution] for collection returning queries using tailable cursors.
     *
     * @author Konrad Kamiński
     */
    open class TailExecution(
            private val operations: CoroutineMongoOperations,
            private val pageable: Pageable
    ) : CoroutineMongoQueryExecution {

        override suspend fun execute(query: Query, type: Class<*>, collection: String): Any =
            operations.tail(query.with(pageable), type, collection)
    }

    open class DeleteExecution(
            private val operations: CoroutineMongoOperations,
            private val method: MongoQueryMethod
    ) : CoroutineMongoQueryExecution {
        /*
		 * (non-Javadoc)
		 * @see org.springframework.data.mongodb.repository.query.AbstractMongoQuery.Execution#execute(org.springframework.data.mongodb.core.query.Query, java.lang.Class, java.lang.String)
		 */
        override suspend fun execute(query: Query, type: Class<*>, collection: String): Any? =
            if (method.isCollectionQuery) {
                operations.findAllAndRemove(query, type, collection)
            } else {
                operations.remove(query, type, collection)?.deletedCount
            }
    }

    /**
     * [CoroutineMongoOperations] to return a single entity.
     *
     * @author Konrad Kamiński
     */
    open class SingleEntityExecution(
            private val operations: CoroutineMongoOperations,
            private val countProjection: Boolean
    ) : CoroutineMongoQueryExecution {

        override suspend fun execute(query: Query, type: Class<*>, collection: String): Any? =
            when (countProjection) {
                true -> operations.count(query, type, collection)
                else -> operations.findOne(query, type, collection)
            }
    }

    open class GeoNearExecution(
            private val operations: CoroutineMongoOperations,
            private val accessor: MongoParameterAccessor,
            private val returnType: TypeInformation<*>
    ) : CoroutineMongoQueryExecution {
        /*
		 * (non-Javadoc)
		 * @see org.springframework.data.mongodb.repository.query.AbstractMongoQuery.Execution#execute(org.springframework.data.mongodb.core.query.Query, java.lang.Class, java.lang.String)
		 */
        override suspend fun execute(query: Query, type: Class<*>, collection: String): Any {
            val results = doExecuteQuery(query, type, collection)

            return if (isStreamOfGeoResult) results else TODO()//results.map { it.content }
        }

        protected suspend fun doExecuteQuery(query: Query?, type: Class<*>, collection: String): ReceiveChannel<GeoResult<out Any>> {

            val nearLocation = accessor.geoNearLocation
            val nearQuery = NearQuery.near(nearLocation)

            if (query != null) {
                nearQuery.query(query)
            }

            val distances = accessor.distanceRange
            distances.upperBound.value.ifPresent { it -> nearQuery.maxDistance(it).`in`(it.metric) }
            distances.lowerBound.value.ifPresent { it -> nearQuery.minDistance(it).`in`(it.metric) }

            val pageable = accessor.pageable

            if (pageable != null) {
                nearQuery.with(pageable)
            }

            return operations.geoNear(nearQuery, type, collection)
        }

        private val isStreamOfGeoResult: Boolean
            get() {

                if (!ReactiveWrappers.supports(returnType.type)) { //TODO: Check if its ReceiveChannel<X>
                    return false
                }

                val componentType = returnType.componentType
                return componentType != null && GeoResult::class.java == componentType.type
            }
    }

    /**
     * A [Converter] to post-process all source objects using the given [ResultProcessor].
     *
     * @author Mark Paluch
     */
    open class ResultProcessingConverter(
            private val processor: ResultProcessor,
            private val operations: CoroutineMongoOperations,
            private val instantiators: EntityInstantiators
    ) : Converter<Any?, Any?> {

        /*
		 * (non-Javadoc)
		 * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
		 */
        override fun convert(source: Any?): Any? {

            val returnedType = processor.returnedType

            if (ClassUtils.isPrimitiveOrWrapper(returnedType.returnedType)) {
                return source
            }

            val converter = DtoInstantiatingConverter(returnedType.returnedType,
                    operations.converter.mappingContext, instantiators)

            return processor.processResult<Any>(source, converter)
        }
    }


    open class ResultProcessingExecution(
        private val delegate: CoroutineMongoQueryExecution,
        private val converter: Converter<Any?, Any?>
    ) : CoroutineMongoQueryExecution {

        override suspend fun execute(query: Query, type: Class<*>, collection: String): Any? {
            val result = this.delegate.execute(query, type, collection)
            return this.converter.convert(result)
        }
    }

    suspend fun execute(query: Query, type: Class<*>, collection: String): Any?
}
