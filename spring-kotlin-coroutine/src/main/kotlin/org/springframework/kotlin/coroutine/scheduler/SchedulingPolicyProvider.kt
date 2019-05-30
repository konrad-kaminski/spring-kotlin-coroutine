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

import org.springframework.context.EmbeddedValueResolverAware
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.util.Assert
import org.springframework.util.StringUtils
import org.springframework.util.StringValueResolver
import java.util.TimeZone

interface SchedulingPolicyProvider {
    fun createSchedulingPolicy(scheduled: Scheduled): SchedulingPolicy
}

internal open class DefaultSchedulingPolicyProvider : SchedulingPolicyProvider, EmbeddedValueResolverAware {
    private var embeddedValueResolver: StringValueResolver? = null

    override fun setEmbeddedValueResolver(resolver: StringValueResolver?) {
        this.embeddedValueResolver = resolver
    }

    override fun createSchedulingPolicy(scheduled: Scheduled): SchedulingPolicy {
        val errorMessage = "Exactly one of the 'cron', 'fixedDelay(String)', or 'fixedRate(String)' attributes is required"
        val initialDelay = scheduled.getEffectiveInitialDelay().let { if (it < 0) null else it }

        return policyCreators.fold(null as SchedulingPolicy?) { currentPolicy, creator ->
            val createdPolicy = creator.invoke(initialDelay, scheduled)

            if (currentPolicy != null && createdPolicy != null) {
                throw IllegalArgumentException(errorMessage)
            }

            currentPolicy ?: createdPolicy
        } ?: throw IllegalArgumentException(errorMessage)
    }

    private fun Scheduled.getEffectiveInitialDelay(): Long =
            getValue("initialDelayString", initialDelayString) {
                Assert.isTrue(initialDelay < 0, "Specify 'initialDelay' or 'initialDelayString', not both")
            } ?: initialDelay

    private fun getValue(strLabel: String, str: String, assertion: () -> Unit = {}): Long? =
            if (str.isNotBlank()) {
                assertion.invoke()

                val effectiveStr = when (embeddedValueResolver != null) {
                    true  -> embeddedValueResolver!!.resolveStringValue(str)
                    false -> str
                }

                if (effectiveStr != null) {
                    try {
                        effectiveStr.toLong()
                    } catch (ex: NumberFormatException) {
                        throw IllegalArgumentException("Invalid $strLabel value \"$effectiveStr\" - cannot parse into integer")
                    }
                } else {
                    throw IllegalArgumentException("Null $strLabel value.")
                }
            } else { null }

    private val CRON_POLICY_CREATOR = { initialDelay: Long?, scheduled: Scheduled ->
        takeIf { scheduled.cron.isNotBlank() }?.let {
            Assert.isTrue(initialDelay == null, "'initialDelay' not supported for cron triggers")

            val (cronValue, zoneValue) =
                    if (embeddedValueResolver != null) {
                        embeddedValueResolver!!.resolveStringValue(scheduled.cron) to
                                embeddedValueResolver!!.resolveStringValue(scheduled.zone)
                    } else {
                        scheduled.cron to scheduled.zone
                    }

            val timeZone = when (zoneValue.isNotBlank()) {
                true -> StringUtils.parseTimeZoneString(zoneValue)
                else -> TimeZone.getDefault()
            }

            CronPolicy(cronValue, timeZone)
        }
    }

    private val FIXED_DELAY_POLICY_CREATOR = { initialDelay: Long?, scheduled: Scheduled ->
        takeIf { scheduled.fixedDelay >= 0 }?.let {
            FixedDelayPolicy(initialDelay ?: 0, scheduled.fixedDelay)
        }
    }

    private val FIXED_DELAY_STRING_POLICY_CREATOR = { initialDelay: Long?, scheduled: Scheduled ->
        getValue("fixedDelayString", scheduled.fixedDelayString)?.let {
            FixedDelayPolicy(initialDelay ?: 0, it)
        }
    }

    private val FIXED_RATE_POLICY_CREATOR = { initialDelay: Long?, scheduled: Scheduled -> //TOOD
        takeIf { scheduled.fixedRate >= 0 }?.let {
            FixedRatePolicy(initialDelay ?: 0, scheduled.fixedRate)
        }
    }

    private val FIXED_RATE_STRING_POLICY_CREATOR = { initialDelay: Long?, scheduled: Scheduled -> //TOOD
        getValue("fixedRateString", scheduled.fixedRateString)?.let {
            FixedRatePolicy(initialDelay ?: 0, it)
        }
    }

    private val policyCreators = sequenceOf(
            CRON_POLICY_CREATOR, FIXED_DELAY_POLICY_CREATOR, FIXED_DELAY_STRING_POLICY_CREATOR,
            FIXED_RATE_POLICY_CREATOR, FIXED_RATE_STRING_POLICY_CREATOR)
}