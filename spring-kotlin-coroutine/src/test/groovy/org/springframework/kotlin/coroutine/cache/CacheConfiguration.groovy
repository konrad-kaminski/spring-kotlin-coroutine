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

package org.springframework.kotlin.coroutine.cache

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.ehcache.EhCacheCacheManager
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource

@EnableCaching
class CacheConfiguration {
    @Bean
    CachedService cachedService() {
        return new CachedService()
    }

    @Bean
    CacheManager cacheManager(net.sf.ehcache.CacheManager cacheManager) {
        return new EhCacheCacheManager(cacheManager)
    }

    @Bean
    EhCacheManagerFactoryBean ehCacheManager() {
        def manager = new EhCacheManagerFactoryBean()

        manager.setConfigLocation(new ClassPathResource("ehcache-test.xml"))
        manager.setShared(true)

        return manager
    }
}

