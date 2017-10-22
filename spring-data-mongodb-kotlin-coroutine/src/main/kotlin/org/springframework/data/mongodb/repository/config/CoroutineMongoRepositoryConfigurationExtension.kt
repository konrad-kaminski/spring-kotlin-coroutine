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

package org.springframework.data.mongodb.repository.config

import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.data.config.ParsingUtils
import org.springframework.data.mongodb.core.mapping.CoroutineDocument
import org.springframework.data.mongodb.repository.CoroutineMongoRepository
import org.springframework.data.mongodb.repository.support.CoroutineMongoRepositoryFactoryBean
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource
import org.springframework.data.repository.config.XmlRepositoryConfigurationSource
import org.springframework.data.repository.core.RepositoryMetadata

open class CoroutineMongoRepositoryConfigurationExtension: MongoRepositoryConfigurationExtension() {

    private val MONGO_TEMPLATE_REF = "coroutine-mongo-template-ref"
    private val CREATE_QUERY_INDEXES = "create-query-indexes"

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getModuleName()
	 */
    override fun getModuleName(): String = "Coroutine MongoDB"

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.config.RepositoryConfigurationExtension#getRepositoryFactoryClassName()
	 */
    fun getRepositoryFactoryClassName(): String =
        CoroutineMongoRepositoryFactoryBean::class.java.name

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getIdentifyingTypes()
	 */
    override fun getIdentifyingTypes(): Collection<Class<*>> =
        setOf(CoroutineMongoRepository::class.java)

    override fun getIdentifyingAnnotations(): Collection<Class<out Annotation>> =
        setOf(CoroutineDocument::class.java)

    /*
         * (non-Javadoc)
         * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#postProcess(org.springframework.beans.factory.support.BeanDefinitionBuilder, org.springframework.data.repository.config.XmlRepositoryConfigurationSource)
         */
    override fun postProcess(builder: BeanDefinitionBuilder, config: XmlRepositoryConfigurationSource) {
        val element = config.element

        ParsingUtils.setPropertyReference(builder, element, MONGO_TEMPLATE_REF, "coroutineMongoOperations")
        ParsingUtils.setPropertyValue(builder, element, CREATE_QUERY_INDEXES, "createIndexesForQueryMethods")
    }

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#postProcess(org.springframework.beans.factory.support.BeanDefinitionBuilder, org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource)
	 */
    override fun postProcess(builder: BeanDefinitionBuilder, config: AnnotationRepositoryConfigurationSource) {
        val attributes = config.attributes

        builder.addPropertyReference("coroutineMongoOperations", attributes.getString("coroutineMongoTemplateRef"))
        builder.addPropertyValue("createIndexesForQueryMethods", attributes.getBoolean("createIndexesForQueryMethods"))
    }

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#useRepositoryConfiguration(org.springframework.data.repository.core.RepositoryMetadata)
	 */
    override fun useRepositoryConfiguration(metadata: RepositoryMetadata): Boolean =
            CoroutineMongoRepository::class.java.isAssignableFrom(metadata.repositoryInterface)
}
