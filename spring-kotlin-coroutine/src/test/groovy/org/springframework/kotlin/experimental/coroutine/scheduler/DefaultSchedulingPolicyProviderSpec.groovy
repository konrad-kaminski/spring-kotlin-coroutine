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

package org.springframework.kotlin.experimental.coroutine.scheduler

import org.springframework.scheduling.annotation.Scheduled
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.annotation.Annotation

class DefaultSchedulingPolicyProviderSpec extends Specification {
    def provider = new DefaultSchedulingPolicyProvider()

    @Unroll
    def "should create proper policy for @Scheduled with #scheduledConfiguration"() {
        given:
        def scheduled = new TestScheduled(scheduledConfiguration)

        when:
        def policy = provider.createSchedulingPolicy(scheduled)

        then:
        policy == expectedPolicy

        where:
        scheduledConfiguration                           | expectedPolicy
        [fixedDelay: 1]                                  | new FixedDelayPolicy(0, 1)
        [initialDelay: 2, fixedDelay: 1]                 | new FixedDelayPolicy(2, 1)
        [initialDelayString: "4", fixedDelay: 1]         | new FixedDelayPolicy(4, 1)
        [initialDelayString: "4", fixedDelayString: "6"] | new FixedDelayPolicy(4, 6)
        [fixedRate: 1]                                   | new FixedRatePolicy(0, 1)
        [initialDelay: 2, fixedRate: 1]                  | new FixedRatePolicy(2, 1)
        [initialDelayString: "4", fixedRate: 1]          | new FixedRatePolicy(4, 1)
        [initialDelayString: "4", fixedRateString: "6"]  | new FixedRatePolicy(4, 6)
        [cron: "0/1 * * * * *"]                          | new CronPolicy("0/1 * * * * *", TimeZone.getDefault())
    }

    @Unroll
    def "should fail creating proper policy for @Scheduled with #scheduledConfiguration"() {
        given:
        def scheduled = new TestScheduled(scheduledConfiguration)

        when:
        provider.createSchedulingPolicy(scheduled)

        then:
        thrown(IllegalArgumentException)

        where:
        scheduledConfiguration << [
            [:],
            [initialDelay: 4],
            [initialDelay: 4, initialDelayString: "6", fixedDelay: 8],
            [initialDelayString: "aaa", fixedDelay: 8],
            [fixedDelay: 3, fixedDelayString: "7"],
            [fixedDelayString: "aaa"],
            [fixedRate: 3, fixedRateString: "7"],
            [fixedRateString: "aaa"],
            [fixedDelay: 2, fixedRate: 5],
            [cron: "0/1 * * * *"],
            [cron: "0/1 * * * * *", zone: "Europe/Warsawx"],
            [initialDelay: 3, cron: "0/1 * * * * *"]
        ]
    }
}

class TestScheduled implements Scheduled {
    static private final Map defaultArgs = [
        cron: "",
        zone: "",
        fixedDelayString: "",
        fixedRateString: "",
        initialDelayString: "",
        fixedDelay: -1L,
        fixedRate: -1L,
        initialDelay: -1L
    ]

    private final String cron;
    private final String zone;
    private final String fixedDelayString;
    private final String fixedRateString;
    private final String initialDelayString;
    private final long fixedDelay;
    private final long fixedRate;
    private final long initialDelay;

    TestScheduled(Map _args) {
        def args = defaultArgs + _args

        cron = args["cron"]
        zone = args["zone"]
        fixedDelayString = args["fixedDelayString"]
        fixedRateString = args["fixedRateString"]
        initialDelayString = args["initialDelayString"]
        fixedDelay = args["fixedDelay"]
        fixedRate = args["fixedRate"]
        initialDelay = args["initialDelay"]
    }

    @Override
    String cron() { return cron }

    @Override
    String zone() { return zone }

    @Override
    long fixedDelay() { return fixedDelay }

    @Override
    String fixedDelayString() { return fixedDelayString }

    @Override
    long fixedRate() { return fixedRate }

    @Override
    String fixedRateString() { return fixedRateString }

    @Override
    long initialDelay() { return initialDelay }

    @Override
    String initialDelayString() { return initialDelayString }

    @Override
    Class<? extends Annotation> annotationType() { return Scheduled.class }
}
