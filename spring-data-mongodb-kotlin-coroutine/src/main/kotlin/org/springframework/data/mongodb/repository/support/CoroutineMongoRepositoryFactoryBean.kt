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
import org.springframework.data.repository.Repository
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import org.springframework.util.Assert
import java.io.Serializable

open class CoroutineMongoRepositoryFactoryBean<T: Repository<S, ID>, S, ID: Serializable>(
        repositoryInterface: Class<T>
): RepositoryFactoryBeanSupport<T, S, ID>(repositoryInterface) {

    private lateinit var operations: CoroutineMongoOperations
    private var createIndexesForQueryMethods = false
    private var mappingContextConfigured = false

    /**
     * Configures the [CoroutineMongoOperations] to be used.
     *
     * @param operations the operations to set
     */
    fun setCoroutineMongoOperations(operations: CoroutineMongoOperations) {
        this.operations = operations
    }

    /**
     * Configures whether to automatically create indexes for the properties referenced in a query method.
     *
     * @param createIndexesForQueryMethods the createIndexesForQueryMethods to set
     */
    fun setCreateIndexesForQueryMethods(createIndexesForQueryMethods: Boolean) {
        this.createIndexesForQueryMethods = createIndexesForQueryMethods
    }

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport#setMappingContext(org.springframework.data.mapping.context.MappingContext)
	 */
    override fun setMappingContext(mappingContext: MappingContext<*, *>) {
        super.setMappingContext(mappingContext)
        this.mappingContextConfigured = true
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.data.repository.support.RepositoryFactoryBeanSupport
	 * #createRepositoryFactory()
	 */
     override fun createRepositoryFactory(): RepositoryFactorySupport =
        getFactoryInstance(operations).apply {
            if (createIndexesForQueryMethods) {
                addQueryCreationListener(IndexEnsuringQueryCreationListener { collectionName -> operations.indexOps(collectionName).blocking() })
            }
        }

    /**
     * Creates and initializes a [CoroutineFactorySupport] instance.
     *
     * @param operations
     * @return
     */
    private fun getFactoryInstance(operations: CoroutineMongoOperations): RepositoryFactorySupport =
        CoroutineMongoRepositoryFactory(operations)

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.data.repository.support.RepositoryFactoryBeanSupport
	 * #afterPropertiesSet()
	 */
    override fun afterPropertiesSet() {
        super.afterPropertiesSet()
        Assert.notNull(operations, "ReactiveMongoOperations must not be null!")

        if (!mappingContextConfigured) {
            setMappingContext(operations.converter.mappingContext)
        }
    }
}