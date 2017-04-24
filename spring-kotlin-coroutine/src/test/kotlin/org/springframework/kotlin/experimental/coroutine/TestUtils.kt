package org.springframework.kotlin.experimental.coroutine

import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import java.util.Optional
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.intrinsics.COROUTINE_SUSPENDED

fun <T: Any> runBlocking(lambda: (Continuation<T>) -> Any) = kotlinx.coroutines.experimental.runBlocking {
    suspendCancellableCoroutine<T> { cont ->
        var result: Optional<Any> = Optional.of(COROUTINE_SUSPENDED)

        try {
            result = Optional.ofNullable(lambda(cont))
        }
        catch (e: Throwable) {
            cont.resumeWithException(e)
        }

        if (result != null) {
            val value = result.get()
            if (value != COROUTINE_SUSPENDED) cont.resume(value as T)
        }
    }
}