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

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.handleCoroutineException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendAtomicCancellableCoroutine
import kotlinx.coroutines.suspendCancellableCoroutine
import org.springframework.aop.support.AopUtils
import org.springframework.beans.DirectFieldAccessor
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.kotlin.coroutine.context.GlobalCoroutineContextResolver
import org.springframework.kotlin.coroutine.context.resolver.TaskSchedulerCoroutineContextResolver
import org.springframework.kotlin.coroutine.context.resolver.TaskSchedulerDispatcher
import org.springframework.kotlin.coroutine.isSuspend
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.scheduling.support.TaskUtils
import org.springframework.util.Assert
import org.springframework.util.StringValueResolver
import java.lang.Long.max
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

open internal class CoroutineScheduledAnnotationBeanPostProcessor(
        private val scheduledDispatcherName: String,
        private val contextResolver: GlobalCoroutineContextResolver,
        private val schedulingPolicyProvider: SchedulingPolicyProvider
) : ScheduledAnnotationBeanPostProcessor() {
    private val taskSchedulerCoroutineContextResolver = TaskSchedulerCoroutineContextResolver()
    private var embeddedValueResolver: StringValueResolver? = null
    private val scheduledCoroutines = mutableListOf<ScheduledCoroutine>()
    private val isAlive = AtomicBoolean(true)
    private val jobs = mutableSetOf<Job>()
    private val jobsLock = ReentrantLock()

    override fun setEmbeddedValueResolver(resolver: StringValueResolver) {
        super.setEmbeddedValueResolver(resolver)

        this.embeddedValueResolver = resolver
    }

    override fun processScheduled(scheduled: Scheduled, method: Method, bean: Any) =
            if (method.isSuspend) {
                processScheduledCoroutine(scheduled, method, bean)
            } else {
                super.processScheduled(scheduled, method, bean)
            }

    override fun onApplicationEvent(event: ContextRefreshedEvent?) {
        super.onApplicationEvent(event)

        if (scheduledCoroutines.isNotEmpty()) {
            val context = getSchedulerContext()
            scheduledCoroutines.forEach { coroutine ->
                launchCoroutine(context, coroutine, context.asScheduledCoroutineExceptionHandler())
            }
        }
    }

    override fun destroy() {
        isAlive.set(false)

        jobsLock.withLock {
            jobs.forEach { it.cancel() }
        }

        super.destroy()
    }

    private fun getSchedulerContext(): CoroutineContext =
            contextResolver.resolveContext(scheduledDispatcherName) ?:
                    taskSchedulerCoroutineContextResolver.resolveContext("", getRegistrarTaskScheduler())!!

    private fun launchCoroutine(dispatcher: CoroutineContext, coroutine: ScheduledCoroutine,
                                exceptionHandler: ScheduledCoroutineExceptionHandler): Job =
            GlobalScope.launch(dispatcher, CoroutineStart.DEFAULT) {
                var lastStart: Date? = null
                var delayPeriod = coroutine.policy.getInitialDelay() ?: coroutine.policy.getDelayBeforeNextRun(null, Date())

                while (isActive && delayPeriod != null) {
                    delay(max(0, delayPeriod))

                    if (isActive) {
                        lastStart = Date()
                        try {
                            coroutine.run()
                        } catch(e: Throwable) {
                            exceptionHandler.invoke(coroutineContext, e, null)
                        }
                    }

                    delayPeriod = coroutine.policy.getDelayBeforeNextRun(lastStart, Date())
                }
            }.apply {
                val registered = registerCoroutineJob(this)

                if (registered) {
                    invokeOnCompletion {
                        unregisterCoroutineJob(this)
                    }

                    start()
                } else {
                    cancel()
                }
            }

    private fun unregisterCoroutineJob(job: Job) = doIfNotDestroyedWithJobsLock {
        jobs.remove(job)
    }

    private fun registerCoroutineJob(job: Job): Boolean = doIfNotDestroyedWithJobsLock {
        jobs.add(job)
    }

    private fun doIfNotDestroyedWithJobsLock(fn: () -> Unit) = jobsLock.withLock {
        isAlive.get().apply {
            if (this) { fn() }
        }
    }

    private fun processScheduledCoroutine(scheduled: Scheduled, method: Method, bean: Any) {
        Assert.isTrue(method.parameterTypes.size == 1,
                "Only no-arg coroutine functions may be annotated with @Scheduled")

        val invocableMethod = AopUtils.selectInvocableMethod(method, bean.javaClass)
        val schedulingPolicy = scheduled.toSchedulingPolicy(method.name)

        scheduledCoroutines += ScheduledCoroutine(bean, invocableMethod, schedulingPolicy)
    }

    private fun Scheduled.toSchedulingPolicy(methodName: String): SchedulingPolicy =
        try {
            schedulingPolicyProvider.createSchedulingPolicy(this)
        } catch (ex: IllegalArgumentException) {
            throw IllegalStateException(
                    "Encountered invalid @Scheduled method '$methodName': ${ex.message}", ex)
        }

}
@UseExperimental(InternalCoroutinesApi::class)
data class ScheduledCoroutine(
        val bean: Any,
        val invocableMethod: Method,
        val policy: SchedulingPolicy
) {
    suspend fun run(): Unit = suspendAtomicCancellableCoroutine { continuation ->
        try {
            invocableMethod.invoke(bean, continuation)
        } catch (e: InvocationTargetException) {
            continuation.resumeWithException(e.targetException)
            null
        } catch (e: Throwable) {
            continuation.resumeWithException(e)
            null
        }?.let {
            if (it != COROUTINE_SUSPENDED) {
                continuation.resume(Unit)
            }
        }
    }
}

typealias ScheduledCoroutineExceptionHandler = (context: CoroutineContext, exception: Throwable, caller: Job?) -> Unit

@UseExperimental(InternalCoroutinesApi::class)
private fun CoroutineContext.asScheduledCoroutineExceptionHandler(): ScheduledCoroutineExceptionHandler =
        when (this) {
            is TaskSchedulerDispatcher -> { _, exception, _ ->
                TaskUtils.getDefaultErrorHandler(true).handleError(exception)
            }
            else                       -> ::handleCoroutineException
        }

private fun ScheduledAnnotationBeanPostProcessor.getRegistrarTaskScheduler(): TaskScheduler =
        (DirectFieldAccessor(this).getPropertyValue("registrar") as ScheduledTaskRegistrar).scheduler
