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

import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.CoroutineMongoOperations
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.repository.query.EvaluationContextProvider
import org.springframework.expression.spel.standard.SpelExpressionParser
import java.util.ArrayList

open class CoroutineStringBasedMongoQuery(
        query: String, method: CoroutineMongoQueryMethod, operations: CoroutineMongoOperations,
        expressionParser: SpelExpressionParser, evaluationContextProvider: EvaluationContextProvider
): AbstractCoroutineMongoQuery(method, operations) {

    private val COUND_AND_DELETE = "Manually defined query for %s cannot be both a count and delete query at the same time!"
    private val LOG = LoggerFactory.getLogger(CoroutineStringBasedMongoQuery::class.java)
    private val BINDING_PARSER = StringBasedMongoQuery.ParameterBindingParser.INSTANCE

    private val query: String
    private val fieldSpec: String?
    private val isCountQuery: Boolean
    private val isDeleteQuery: Boolean
    private val queryParameterBindings: List<StringBasedMongoQuery.ParameterBinding>
    private val fieldSpecParameterBindings: List<StringBasedMongoQuery.ParameterBinding>
    private val parameterBinder: ExpressionEvaluatingParameterBinder

    constructor(method: CoroutineMongoQueryMethod, operations: CoroutineMongoOperations,
                expressionParser: SpelExpressionParser, evaluationContextProvider: EvaluationContextProvider):
            this(method.annotatedQuery, method, operations, expressionParser, evaluationContextProvider)

    init {
        this.queryParameterBindings = ArrayList()
        this.query = BINDING_PARSER.parseAndCollectParameterBindingsFromQueryIntoBindings(query,
                this.queryParameterBindings)

        this.fieldSpecParameterBindings = ArrayList()
        this.fieldSpec = BINDING_PARSER.parseAndCollectParameterBindingsFromQueryIntoBindings(
                method.fieldSpecification, this.fieldSpecParameterBindings)

        this.isCountQuery = method.hasAnnotatedQuery() && method.queryAnnotation.count
        this.isDeleteQuery = method.hasAnnotatedQuery() && method.queryAnnotation.delete

        if (isCountQuery && isDeleteQuery) {
            throw IllegalArgumentException(String.format(COUND_AND_DELETE, method))
        }

        this.parameterBinder = ExpressionEvaluatingParameterBinder(expressionParser, evaluationContextProvider)
    }

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.repository.query.AbstractMongoQuery#createQuery(org.springframework.data.mongodb.repository.query.ConvertingParameterAccessor)
	 */
    override fun createQuery(accessor: ConvertingParameterAccessor): Query {

        val queryString = parameterBinder.bind(this.query, accessor,
                ExpressionEvaluatingParameterBinder.BindingContext(queryMethod.parameters, queryParameterBindings))
        val fieldsString = parameterBinder.bind(this.fieldSpec, accessor,
                ExpressionEvaluatingParameterBinder.BindingContext(queryMethod.parameters, fieldSpecParameterBindings))

        return BasicQuery(queryString, fieldsString).with(accessor.sort).apply {
            if (LOG.isDebugEnabled) {
                LOG.debug(String.format("Created query %s for %s fields.", queryObject, fieldsObject))
            }
        }
    }

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.repository.query.AbstractMongoQuery#isCountQuery()
	 */
    override fun isCountQuery() = isCountQuery

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.repository.query.AbstractMongoQuery#isDeleteQuery()
	 */
    override fun isDeleteQuery() = isDeleteQuery
}