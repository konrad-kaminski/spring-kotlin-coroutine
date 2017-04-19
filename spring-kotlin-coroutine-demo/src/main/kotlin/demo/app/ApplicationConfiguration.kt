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

@SpringBootApplication
@EnableCoroutine
@EnableCaching
open class ApplicationConfiguration {
    @Bean
    open fun cacheManager(): CacheManager = EhCacheCacheManager(ehCacheCacheManager().`object`)

    @Bean
    open fun ehCacheCacheManager(): EhCacheManagerFactoryBean =
            EhCacheManagerFactoryBean().apply {
                setConfigLocation(ClassPathResource("ehcache.xml"))
                setShared(true)
            }
}

fun main(vararg args: String) {
    SpringApplication.run(ApplicationConfiguration::class.java, *args)
}