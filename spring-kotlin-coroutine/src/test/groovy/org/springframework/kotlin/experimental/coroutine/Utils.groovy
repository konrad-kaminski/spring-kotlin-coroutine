package org.springframework.kotlin.experimental.coroutine

import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlin.jvm.functions.Function2
import kotlinx.coroutines.experimental.BuildersKt
import kotlinx.coroutines.experimental.CoroutineScope

class Utils {
    static def runBlocking(Function2<Continuation, CoroutineScope, Object> lambda) {
        return BuildersKt.runBlocking(EmptyCoroutineContext, lambda)
    }
}
