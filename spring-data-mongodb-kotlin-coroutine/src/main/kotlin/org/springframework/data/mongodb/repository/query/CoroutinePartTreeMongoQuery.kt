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

import com.mongodb.util.JSONParseException
import org.bson.Document
import org.springframework.data.mongodb.core.CoroutineMongoOperations
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.repository.query.parser.PartTree
import org.springframework.util.StringUtils

open class CoroutinePartTreeMongoQuery(
        method: CoroutineMongoQueryMethod,
        operations: CoroutineMongoOperations
): AbstractCoroutineMongoQuery(method, operations) {

    private val processor = method.resultProcessor
    private val tree = PartTree(method.name, processor.returnedType.domainType)
    private val isGeoNearQuery = method.isGeoNearQuery
    private val context = operations.converter.mappingContext

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.repository.query.AbstractMongoQuery#createQuery(org.springframework.data.mongodb.repository.query.ConvertingParameterAccessor, boolean)
	 */
    override fun createQuery(accessor: ConvertingParameterAccessor): Query {

        val creator = MongoQueryCreator(tree, accessor, context, isGeoNearQuery)
        val query = creator.createQuery()

        if (tree.isLimiting) {
            query.limit(tree.maxResults!!)
        }

        val textCriteria = accessor.fullText
        if (textCriteria != null) {
            query.addCriteria(textCriteria)
        }

        val fieldSpec = queryMethod.fieldSpecification

        if (!StringUtils.hasText(fieldSpec)) {

            val returnedType = processor.withDynamicProjection(accessor).returnedType

            if (returnedType.isProjecting) {
                returnedType.inputProperties.forEach {
                    query.fields().include(it)
                }
            }

            return query
        }

        try {

            val result = BasicQuery(query.queryObject, Document.parse(fieldSpec))
            result.sortObject = query.sortObject

            return result

        } catch (o_O: JSONParseException) {
            throw IllegalStateException(String.format("Invalid query or field specification in %s!", queryMethod),
                    o_O)
        }

    }

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.repository.query.AbstractMongoQuery#createCountQuery(org.springframework.data.mongodb.repository.query.ConvertingParameterAccessor)
	 */
    override fun createCountQuery(accessor: ConvertingParameterAccessor) =
        MongoQueryCreator(tree, accessor, context, false).createQuery()

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.repository.query.AbstractMongoQuery#isCountQuery()
	 */
    override fun isCountQuery() = tree.isCountProjection

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.repository.query.AbstractMongoQuery#isDeleteQuery()
	 */
    override fun isDeleteQuery() = tree.isDelete
}