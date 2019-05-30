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

package org.springframework.kotlin.coroutine.scheduler

import kotlinx.coroutines.delay
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kotlin.coroutine.EnableCoroutine
import org.springframework.kotlin.coroutine.context.DEFAULT_DISPATCHER
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.atomic.AtomicInteger

@EnableScheduling
abstract class BaseConfiguration {
    @Bean
    open fun counter() = AtomicInteger(0)
}

@EnableCoroutine
abstract class DefaultDispatcherConfiguration: BaseConfiguration()

@Configuration
open class FixedDelayConfiguration : DefaultDispatcherConfiguration() {
    @Scheduled(fixedDelay = 100)
    suspend open fun task() {
        counter().incrementAndGet()
    }
}

@Configuration
@EnableCoroutine(schedulerDispatcher = DEFAULT_DISPATCHER)
open class FixedDelayOnDefaultDispatcherConfiguration : BaseConfiguration() {
    @Scheduled(fixedDelay = 100)
    suspend open fun task() {
        if (DEFAULT_DISPATCHER_THREAD_NAME_REGEX.containsMatchIn (Thread.currentThread().name)) {
            counter().incrementAndGet()
        }
    }

    companion object {
        private val DEFAULT_DISPATCHER_THREAD_NAME_REGEX = Regex("^DefaultDispatcher-worker-.+")
    }
}

@Configuration
open class FixedDelayWithSuspensionPointConfiguration : DefaultDispatcherConfiguration() {
    @Scheduled(fixedDelay = 99)
    suspend open fun task() {
        delay(1)
        counter().incrementAndGet()
    }
}

@Configuration
open class FixedDelayStringConfiguration : DefaultDispatcherConfiguration() {
    @Scheduled(fixedDelayString = "100")
    suspend open fun task() {
        counter().incrementAndGet()
    }
}

@Configuration
open class FixedDelayWithInitialDelayConfiguration : DefaultDispatcherConfiguration() {
    @Scheduled(initialDelay = 1000, fixedDelay = 100)
    suspend open fun task() {
        counter().incrementAndGet()
    }
}

@Configuration
open class FixedDelayWithInitialDelayStringConfiguration : DefaultDispatcherConfiguration() {
    @Scheduled(initialDelayString = "1000", fixedDelay = 100)
    suspend open fun task() {
        counter().incrementAndGet()
    }
}

@Configuration
open class FixedRateConfiguration : DefaultDispatcherConfiguration() {
    @Scheduled(fixedRate = 10)
    suspend open fun task() {
        counter().incrementAndGet()
    }
}

@Configuration
open class FixedRateWithExceptionConfiguration : DefaultDispatcherConfiguration() {
    @Scheduled(fixedRate = 10)
    suspend open fun task() {
        counter().incrementAndGet()
        throw RuntimeException("test")
    }
}

@Configuration
open class FixedRateWithSuspensionPointAndExceptionConfiguration : DefaultDispatcherConfiguration() {
    @Scheduled(fixedRate = 10)
    suspend open fun task() {
        delay(1)
        counter().incrementAndGet()
        throw RuntimeException("test")
    }
}

@Configuration
open class FixedRateStringConfiguration : DefaultDispatcherConfiguration() {
    @Scheduled(fixedRateString = "10")
    suspend open fun task() {
        counter().incrementAndGet()
    }
}

@Configuration
open class FixedRateWithInitialDelayConfiguration : DefaultDispatcherConfiguration() {
    @Scheduled(initialDelay = 1000, fixedRate = 100)
    suspend open fun task() {
        counter().incrementAndGet()
    }
}

@Configuration
open class CronConfiguration : DefaultDispatcherConfiguration() {
    @Scheduled(cron = "0/1 * * * * *")
    suspend open fun task() {
        counter().incrementAndGet()
    }
}

@Configuration
open class InvalidConfiguration : DefaultDispatcherConfiguration() {
    @Scheduled(cron = "aaa")
    suspend open fun task() {
    }
}
