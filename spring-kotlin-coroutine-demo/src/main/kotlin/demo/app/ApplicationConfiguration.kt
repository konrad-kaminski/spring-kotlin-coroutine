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

package demo.app

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.ehcache.EhCacheCacheManager
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.kotlin.experimental.coroutine.EnableCoroutine
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableCoroutine
@EnableCaching
@EnableScheduling
open class ApplicationConfiguration {
    @Bean
    open fun cacheManager(): CacheManager = EhCacheCacheManager(ehCacheManager().`object`)

    @Bean
    open fun ehCacheManager(): EhCacheManagerFactoryBean =
            EhCacheManagerFactoryBean().apply {
                setConfigLocation(ClassPathResource("ehcache.xml"))
                setShared(true)
            }
}

fun main(vararg args: String) {
    SpringApplication.run(ApplicationConfiguration::class.java, *args)
}