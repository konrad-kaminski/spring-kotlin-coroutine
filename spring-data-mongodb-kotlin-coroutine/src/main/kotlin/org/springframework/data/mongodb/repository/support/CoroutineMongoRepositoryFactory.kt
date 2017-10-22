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

import org.springframework.data.mapping.context.MappingContext
import org.springframework.data.mongodb.core.CoroutineMongoOperations
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty
import org.springframework.data.mongodb.repository.query.CoroutineMongoQueryMethod
import org.springframework.data.mongodb.repository.query.CoroutinePartTreeMongoQuery
import org.springframework.data.mongodb.repository.query.CoroutineStringBasedMongoQuery
import org.springframework.data.mongodb.repository.query.MongoEntityInformation
import org.springframework.data.mongodb.repository.query.PartTreeMongoQuery
import org.springframework.data.projection.ProjectionFactory
import org.springframework.data.repository.core.NamedQueries
import org.springframework.data.repository.core.RepositoryInformation
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.core.support.MethodInvocationValidator
import org.springframework.data.repository.query.EvaluationContextProvider
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.repository.query.RepositoryQuery
import org.springframework.expression.spel.standard.SpelExpressionParser
import java.io.Serializable
import java.lang.reflect.Method
import java.util.Optional

open class CoroutineMongoRepositoryFactory(
    private val operations: CoroutineMongoOperations
): CoroutineRepositoryFactorySupport() {
    private val mappingContext = operations.converter.mappingContext // MappingContext<out MongoPersistentEntity<*>, MongoPersistentProperty>

    init {
        addRepositoryProxyPostProcessor { factory, _ ->
            factory.advisors
                    .filter { it.advice is MethodInvocationValidator }
                    .forEach { factory.removeAdvisor(it) } //TODO: Fix the advice for suspend methods
        }
    }

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactorySupport#getRepositoryBaseClass(org.springframework.data.repository.core.RepositoryMetadata)
	 */
    override fun getRepositoryBaseClass(metadata: RepositoryMetadata): Class<*> =
        SimpleCoroutineMongoRepository::class.java

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactorySupport#getTargetRepository(org.springframework.data.repository.core.RepositoryInformation)
	 */
    override fun getTargetRepository(information: RepositoryInformation): Any {
        val entityInformation = getEntityInformation<Any, Serializable>(information.domainType as Class<Any>,
                information)

        return getTargetRepositoryViaReflection(information, entityInformation, operations)
    }

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactorySupport#getQueryLookupStrategy(org.springframework.data.repository.query.QueryLookupStrategy.Key, org.springframework.data.repository.query.EvaluationContextProvider)
	 */
    override fun getQueryLookupStrategy(key: QueryLookupStrategy.Key,
                                        evaluationContextProvider: EvaluationContextProvider): Optional<QueryLookupStrategy> =
        Optional.of(MongoQueryLookupStrategy(operations, evaluationContextProvider, mappingContext))

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactorySupport#getEntityInformation(java.lang.Class)
	 */
    override fun <T, ID> getEntityInformation(domainClass: Class<T>): MongoEntityInformation<T, ID> =
            getEntityInformation(domainClass, null)

    private fun <T, ID> getEntityInformation(domainClass: Class<T>,
                                             information: RepositoryInformation?): MongoEntityInformation<T, ID> =
        MappingMongoEntityInformation(
                mappingContext.getRequiredPersistentEntity(domainClass) as MongoPersistentEntity<T>,
                information?.idType as? Class<ID>)

    companion object {
        private val EXPRESSION_PARSER = SpelExpressionParser()
    }

    /**
     * [QueryLookupStrategy] to create [PartTreeMongoQuery] instances.
     *
     * @author Mark Paluch
     */
    private class MongoQueryLookupStrategy(
            private val operations: CoroutineMongoOperations,
            private val evaluationContextProvider: EvaluationContextProvider,
            private val mappingContext: MappingContext<out MongoPersistentEntity<*>, MongoPersistentProperty>
    ) : QueryLookupStrategy {

        /*
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.query.QueryLookupStrategy#resolveQuery(java.lang.reflect.Method, org.springframework.data.repository.core.RepositoryMetadata, org.springframework.data.projection.ProjectionFactory, org.springframework.data.repository.core.NamedQueries)
		 */
        override fun resolveQuery(method: Method, metadata: RepositoryMetadata, factory: ProjectionFactory,
                                  namedQueries: NamedQueries): RepositoryQuery {

            val queryMethod = CoroutineMongoQueryMethod(method, metadata, factory, mappingContext)
            val namedQueryName = queryMethod.namedQueryName

            if (namedQueries.hasQuery(namedQueryName)) {
                val namedQuery = namedQueries.getQuery(namedQueryName)
                return CoroutineStringBasedMongoQuery(namedQuery, queryMethod, operations, EXPRESSION_PARSER,
                        evaluationContextProvider)
            } else return if (queryMethod.hasAnnotatedQuery()) {
                CoroutineStringBasedMongoQuery(queryMethod, operations, EXPRESSION_PARSER, evaluationContextProvider)
            } else {
                CoroutinePartTreeMongoQuery(queryMethod, operations)
            }
        }
    }
}