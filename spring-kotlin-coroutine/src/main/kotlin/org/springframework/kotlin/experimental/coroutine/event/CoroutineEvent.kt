package org.springframework.kotlin.experimental.coroutine.event

import org.springframework.context.ApplicationEvent
import org.springframework.context.PayloadApplicationEvent

internal class CoroutineEvent<T: ApplicationEvent>(
        original: T,
        source: Any,
        private val blocks: MutableList<suspend () -> Unit> = mutableListOf()
) : PayloadApplicationEvent<T>(source, original) {
    fun schedule(block: suspend () -> Unit) { blocks += block }

    suspend fun invokeBlocks() = blocks.forEach { it.invoke() }
}
