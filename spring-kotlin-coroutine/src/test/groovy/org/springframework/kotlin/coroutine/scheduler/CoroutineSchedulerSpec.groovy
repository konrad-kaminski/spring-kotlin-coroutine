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

import org.springframework.beans.factory.BeanCreationException
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.atomic.AtomicInteger

class CoroutineSchedulerSpec extends Specification {
    private ConfigurableApplicationContext ctx

    def cleanup() {
        if (ctx != null) {
            ctx.close()
        }
    }

    @Unroll
    def "should support @Scheduled with fixedDelay using #configuration"() {
        given:
        ctx = new AnnotationConfigApplicationContext(configuration)

        when:
        Thread.sleep(100 + 50)

        then:
        ctx.getBean(AtomicInteger.class).get() >= 1

        where:
        configuration << [FixedDelayConfiguration, FixedDelayStringConfiguration,
                          FixedDelayOnDefaultDispatcherConfiguration, FixedDelayWithSuspensionPointConfiguration]
    }

    @Unroll
    def "should support @Scheduled with fixedDelay and initialDelay using #configuration"() {
        given:
        ctx = new AnnotationConfigApplicationContext(configuration)

        when:
        Thread.sleep(1950)

        then:
        def counter = ctx.getBean(AtomicInteger.class).get()
        counter >= 1
        counter <= 10

        where:
        configuration << [FixedDelayWithInitialDelayConfiguration, FixedDelayWithInitialDelayStringConfiguration,
                          FixedDelayStringWithInitialDelayStringConfiguration]
    }

    @Unroll
    def "should support @Scheduled with fixedRate using #configuration"() {
        given:
        ctx = new AnnotationConfigApplicationContext(configuration)

        when:
        Thread.sleep(100 + 50)

        then:
        ctx.getBean(AtomicInteger.class).get() >= 10

        where:
        configuration << [FixedRateConfiguration, FixedRateWithExceptionConfiguration,
                          FixedRateWithSuspensionPointAndExceptionConfiguration, FixedRateStringConfiguration]
    }

    def "should support @Scheduled with fixedRate and initialDelay"() {
        given:
        ctx = new AnnotationConfigApplicationContext(FixedRateWithInitialDelayConfiguration)

        when:
        Thread.sleep(1950)

        then:
        def counter = ctx.getBean(AtomicInteger.class).get()
        counter >= 0
        counter <= 10
    }

    def "should support @Scheduled with cron"() {
        given:
        ctx = new AnnotationConfigApplicationContext(CronConfiguration)

        when:
        Thread.sleep(2000 + 50)

        then:
        def counter = ctx.getBean(AtomicInteger.class).get()
        counter >= 1
    }

    def "should fail loading context with incorrect @Scheduled"() {
        when:
        ctx = new AnnotationConfigApplicationContext(InvalidConfiguration)

        then:
        thrown(BeanCreationException)
    }
}
