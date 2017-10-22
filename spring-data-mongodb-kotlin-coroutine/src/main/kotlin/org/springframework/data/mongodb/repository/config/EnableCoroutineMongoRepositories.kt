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

import org.springframework.beans.factory.FactoryBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.support.CoroutineMongoRepositoryFactoryBean
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean
import org.springframework.data.repository.config.DefaultRepositoryBaseClass
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.repository.query.QueryLookupStrategy.Key
import java.lang.annotation.Documented
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
@Documented
@Inherited
@Import(CoroutineMongoRepositoriesRegistrar::class)
annotation class EnableCoroutineMongoRepositories(
        /**
         * Alias for the [.basePackages] attribute. Allows for more concise annotation declarations e.g.:
         * `@EnableCoroutineMongoRepositories("org.my.pkg")` instead of
         * `@EnableCoroutineMongoRepositories(basePackages="org.my.pkg")`.
         */
        val value: Array<String> = arrayOf(),
        /**
         * Base packages to scan for annotated components. [.value] is an alias for (and mutually exclusive with) this
         * attribute. Use [.basePackageClasses] for a type-safe alternative to String-based package names.
         */
        val basePackages: Array<String> = arrayOf(),
        /**
         * Type-safe alternative to [.basePackages] for specifying the packages to scan for annotated components. The
         * package of each class specified will be scanned. Consider creating a special no-op marker class or interface in
         * each package that serves no purpose other than being referenced by this attribute.
         */
        val basePackageClasses: Array<KClass<*>> = arrayOf(),
        /**
         * Specifies which types are eligible for component scanning. Further narrows the set of candidate components from
         * everything in [.basePackages] to everything in the base packages that matches the given filter or filters.
         */
        val includeFilters: Array<ComponentScan.Filter> = arrayOf(),
        /**
         * Specifies which types are not eligible for component scanning.
         */
        val excludeFilters: Array<ComponentScan.Filter> = arrayOf(),
        /**
         * Returns the postfix to be used when looking up custom repository implementations. Defaults to Impl. So
         * for a repository named `PersonRepository` the corresponding implementation class will be looked up scanning
         * for `PersonRepositoryImpl`.
         *
         * @return
         */
        val repositoryImplementationPostfix: String = "Impl",
        /**
         * Configures the location of where to find the Spring Data named queries properties file. Will default to
         * `META-INF/mongo-named-queries.properties`.
         *
         * @return
         */
        val namedQueriesLocation: String = "",
        /**
         * Returns the key of the [QueryLookupStrategy] to be used for lookup queries for query methods. Defaults to
         * [Key.CREATE_IF_NOT_FOUND].
         *
         * @return
         */
        val queryLookupStrategy: Key = Key.CREATE_IF_NOT_FOUND,
        /**
         * Returns the [FactoryBean] class to be used for each repository instance. Defaults to
         * [MongoRepositoryFactoryBean].
         *
         * @return
         */
        val repositoryFactoryBeanClass: KClass<*> = CoroutineMongoRepositoryFactoryBean::class,
        /**
         * Configure the repository base class to be used to create repository proxies for this particular configuration.
         *
         * @return
         */
        val repositoryBaseClass: KClass<*> = DefaultRepositoryBaseClass::class,
        /**
         * Configures the name of the [MongoTemplate] bean to be used with the repositories detected.
         *
         * @return
         */
        val coroutineMongoTemplateRef: String = "coroutineMongoTemplate",
        /**
         * Whether to automatically create indexes for query methods defined in the repository interface.
         *
         * @return
         */
        val createIndexesForQueryMethods: Boolean = false,
        /**
         * Configures whether nested repository-interfaces (e.g. defined as inner classes) should be discovered by the
         * repositories infrastructure.
         */
        val considerNestedRepositories: Boolean = false
)
