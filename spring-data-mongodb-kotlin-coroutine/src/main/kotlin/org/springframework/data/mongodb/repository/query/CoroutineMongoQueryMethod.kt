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

import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Sort
import org.springframework.data.geo.GeoResult
import org.springframework.data.mapping.context.MappingContext
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty
import org.springframework.data.projection.ProjectionFactory
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.query.QueryMethod
import org.springframework.data.repository.util.ClassUtils.hasParameterOfType
import org.springframework.data.repository.util.ReactiveWrappers
import org.springframework.data.util.ClassTypeInformation
import org.springframework.kotlin.experimental.coroutine.isCoroutineCollection
import org.springframework.kotlin.experimental.coroutine.returnTypeMetadata
import org.springframework.util.ClassUtils
import java.lang.reflect.Method
import kotlin.coroutines.experimental.Continuation

open class CoroutineMongoQueryMethod(
        val method: Method,
        metadata: RepositoryMetadata, projectionFactory: ProjectionFactory,
        mappingContext: MappingContext<out MongoPersistentEntity<*>, MongoPersistentProperty>
): MongoQueryMethod(method, metadata, projectionFactory, mappingContext) {

    private val PAGE_TYPE = ClassTypeInformation.from(Page::class.java)
    private val SLICE_TYPE = ClassTypeInformation.from(Slice::class.java)

    /**
     * Creates a new [CoroutineMongoQueryMethod] from the given [Method].
     *
     * @param method must not be null.
     * @param metadata must not be null.
     * @param projectionFactory must not be null.
     * @param mappingContext must not be null.
     */
    init {
        if (hasParameterOfType(method, Pageable::class.java)) {

            val returnType = ClassTypeInformation.fromReturnTypeOf<Any>(method)

            val multiWrapper = ReactiveWrappers.isMultiValueType(returnType.type) //TODO:
            val singleWrapperWithWrappedPageableResult = ReactiveWrappers.isSingleValueType(returnType.type) && (PAGE_TYPE.isAssignableFrom(returnType.requiredComponentType) || SLICE_TYPE.isAssignableFrom(returnType.requiredComponentType))

            if (singleWrapperWithWrappedPageableResult) {
                throw InvalidDataAccessApiUsageException(
                        String.format("'%s.%s' must not use sliced or paged execution. Please use Flux.buffer(size, skip).",
                                ClassUtils.getShortName(method.declaringClass), method.name))
            }

            if (!multiWrapper && !singleWrapperWithWrappedPageableResult) {
                throw IllegalStateException(String.format(
                        "Method has to use a either multi-item reactive wrapper return type or a wrapped Page/Slice type. Offending method: %s",
                        method.toString()))
            }

            if (hasParameterOfType(method, Sort::class.java)) {
                throw IllegalStateException(String.format("Method must not have Pageable *and* Sort parameter. " + "Use sorting capabilities on Pageble instead! Offending method: %s", method.toString()))
            }
        }
    }

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.repository.query.MongoQueryMethod#createParameters(java.lang.reflect.Method)
	 */
    override fun createParameters(method: Method): MongoParameters =
            CoroutineMongoParameters(method, isGeoNearQuery(method))

    open fun getReturnTypeMetadata() = method.returnTypeMetadata

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.query.QueryMethod#isCollectionQuery()
	 */
    override fun isCollectionQuery(): Boolean =
            !(isPageQuery || isSliceQuery) && method.returnTypeMetadata.clazz.isCoroutineCollection

    fun isSuspend(): Boolean =
        method.parameterCount > 0 &&
        Continuation::class.java.isAssignableFrom(method.parameterTypes.last())

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.repository.query.MongoQueryMethod#isGeoNearQuery()
	 */
    override fun isGeoNearQuery(): Boolean = isGeoNearQuery(method)

    private fun isGeoNearQuery(method: Method): Boolean {

        if (ReactiveWrappers.supports(method.returnType)) {
            val from = ClassTypeInformation.fromReturnTypeOf<Any>(method)
            return GeoResult::class.java == from.requiredComponentType.type
        }

        return false
    }

    /*
	 * All reactive query methods are streaming queries.
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.query.QueryMethod#isStreamQuery()
	 */
    override fun isStreamQuery(): Boolean = true

    override fun getReturnedObjectType(): Class<*> = methodField.returnTypeMetadata.domainClazz

    private val QueryMethod.methodField: Method
        get() = QueryMethod::class.java.getDeclaredField("method").apply {
            isAccessible = true
        }.get(this) as Method
}

