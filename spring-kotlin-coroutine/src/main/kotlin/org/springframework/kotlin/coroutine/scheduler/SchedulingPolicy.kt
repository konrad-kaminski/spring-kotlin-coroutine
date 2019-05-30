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

import org.springframework.scheduling.support.CronSequenceGenerator
import java.lang.Long.max
import java.util.Date
import java.util.TimeZone

interface SchedulingPolicy {
    fun getInitialDelay(): Long?

    fun getDelayBeforeNextRun(lastStartExecutionDate: Date?, currentDate: Date): Long?
}

data class FixedRatePolicy(
        val initialDelay: Long, val fixedRate: Long
) : SchedulingPolicy {
    override fun getInitialDelay() = initialDelay
    override fun getDelayBeforeNextRun(lastStartExecutionDate: Date?, currentDate: Date): Long? =
            lastStartExecutionDate?.let {
                max(it.time + fixedRate, currentDate.time) - currentDate.time
            } ?: 0
}

data class CronPolicy(
        val cronExpression: String,
        val timeZone: TimeZone
) : SchedulingPolicy {
    private val cronGenerator = CronSequenceGenerator(cronExpression, timeZone)

    override fun getInitialDelay() = null
    override fun getDelayBeforeNextRun(lastStartExecutionDate: Date?, currentDate: Date) = cronGenerator.next(currentDate)?.let { it.time - System.currentTimeMillis() }
}

data class FixedDelayPolicy(
        val initialDelay: Long,
        val fixedDelay: Long
) : SchedulingPolicy {
    override fun getInitialDelay() = initialDelay
    override fun getDelayBeforeNextRun(lastStartExecutionDate: Date?, currentDate: Date) = fixedDelay
}
