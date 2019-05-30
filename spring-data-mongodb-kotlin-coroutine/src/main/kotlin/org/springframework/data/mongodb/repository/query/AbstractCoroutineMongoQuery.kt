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

import kotlinx.coroutines.runBlocking
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.EntityInstantiators
import org.springframework.data.mongodb.core.CoroutineMongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.repository.query.ParameterAccessor
import org.springframework.data.repository.query.RepositoryQuery
import org.springframework.kotlin.coroutine.util.executeSuspend
import kotlin.coroutines.Continuation

abstract class AbstractCoroutineMongoQuery(
    private val method: CoroutineMongoQueryMethod,
    private val operations: CoroutineMongoOperations
): RepositoryQuery {

    private val instantiators: EntityInstantiators = EntityInstantiators()

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.query.RepositoryQuery#getQueryMethod()
	 */
    override fun getQueryMethod(): MongoQueryMethod = method

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.query.RepositoryQuery#execute(java.lang.Object[])
	 */
    override fun execute(parameters: Array<Any>): Any? =
        if (method.isSuspend()) {
            executeSuspend(parameters.last() as Continuation<Any?>) {
                execute(MongoParametersParameterAccessor(method, parameters))
            }
        } else {
            runBlocking {
                execute(MongoParametersParameterAccessor(method, parameters))
            }
        }

    private suspend fun execute(parameterAccessor: MongoParameterAccessor): Any? {

        val query = createQuery(ConvertingParameterAccessor(operations.converter, parameterAccessor))

        applyQueryMetaAttributesWhenPresent(query)

        val processor = method.resultProcessor.withDynamicProjection(parameterAccessor)
        val collection = method.entityInformation.collectionName

        val execution = getExecution(query, parameterAccessor,
                CoroutineMongoQueryExecution.ResultProcessingConverter(processor, operations, instantiators))

        return execution.execute(query, processor.returnedType.domainType, collection)
    }

    /**
     * Returns the execution instance to use.
     *
     * @param query must not be null.
     * @param accessor must not be null.
     * @param resultProcessing must not be null.
     * @return
     */
    private fun getExecution(query: Query, accessor: MongoParameterAccessor,
                             resultProcessing: Converter<Any?, Any?>): CoroutineMongoQueryExecution =
        CoroutineMongoQueryExecution.ResultProcessingExecution(getExecutionToWrap(accessor), resultProcessing)

    private fun getExecutionToWrap(accessor: MongoParameterAccessor): CoroutineMongoQueryExecution =
        if (isDeleteQuery()) {
            CoroutineMongoQueryExecution.DeleteExecution(operations, method)
        } else if (method.isGeoNearQuery) {
            CoroutineMongoQueryExecution.GeoNearExecution(operations, accessor, method.returnType)
        } else if (isTailable(method)) {
            CoroutineMongoQueryExecution.TailExecution(operations, accessor.pageable)
        } else if (method.isCollectionQuery) {
            CoroutineMongoQueryExecution.CollectionExecution(operations, accessor.pageable, method.getReturnTypeMetadata())
        } else {
            CoroutineMongoQueryExecution.SingleEntityExecution(operations, isCountQuery())
        }

    private fun isTailable(method: MongoQueryMethod): Boolean =
        method.tailableAnnotation != null

    private fun applyQueryMetaAttributesWhenPresent(query: Query): Query = query.apply {
        if (method.hasQueryMetaAttributes()) {
            meta = method.queryMetaAttributes
        }
    }

    /**
     * Creates a [Query] instance using the given [ConvertingParameterAccessor]. Will delegate to
     * [.createQuery] by default but allows customization of the count query to be
     * triggered.
     *
     * @param accessor must not be null.
     * @return
     */
    open protected fun createCountQuery(accessor: ConvertingParameterAccessor): Query {
        return applyQueryMetaAttributesWhenPresent(createQuery(accessor))
    }

    /**
     * Creates a [Query] instance using the given [ParameterAccessor]
     *
     * @param accessor must not be null.
     * @return
     */
    protected abstract fun createQuery(accessor: ConvertingParameterAccessor): Query

    /**
     * Returns whether the query should get a count projection applied.
     *
     * @return
     */
    protected abstract fun isCountQuery(): Boolean

    /**
     * Return weather the query should delete matching documents.
     *
     * @return
     * @since 1.5
     */
    protected abstract fun isDeleteQuery(): Boolean
}
