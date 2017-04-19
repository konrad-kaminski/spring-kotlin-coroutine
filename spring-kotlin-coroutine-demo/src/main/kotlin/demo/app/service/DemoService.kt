package demo.app.service

import kotlinx.coroutines.experimental.delay
import org.springframework.cache.annotation.Cacheable
import demo.app.util.logger
import org.springframework.kotlin.experimental.coroutine.annotation.Coroutine
import org.springframework.kotlin.experimental.coroutine.context.COMMON_POOL
import org.springframework.stereotype.Component

@Component
open class DemoService {
    suspend open fun delayedReturn(s: String, delayMillis: Long): String {
        logger.info ("Before delay in [delayedReturn]")
        delay(delayMillis)
        logger.info ("After delay in [delayedReturn]")

        return s
    }

    @Coroutine(COMMON_POOL)
    suspend open fun commmonPoolReturn(s: String): String {
        logger.info ("In [commmonPoolReturn]")

        return s
    }

    @Cacheable("cache1")
    suspend open fun cachedDelayedReturn(s: String, delayMillis: Long): String {
        logger.info ("Before delay in [cachedDelayedReturn]")
        delay(delayMillis)
        logger.info ("After delay in [cachedDelayedReturn]")

        return s
    }

    @Cacheable("cache2")
    @Coroutine(COMMON_POOL)
    suspend open fun cachedCommonPoolDelayedReturn(s: String, delayMillis: Long): String {
        logger.info ("Before delay in [cachedCommonPoolDelayedReturn]")
        delay(delayMillis)
        logger.info ("After delay in [cachedCommonPoolDelayedReturn]")

        return s
    }

    companion object {
        private val logger = logger()
    }
}