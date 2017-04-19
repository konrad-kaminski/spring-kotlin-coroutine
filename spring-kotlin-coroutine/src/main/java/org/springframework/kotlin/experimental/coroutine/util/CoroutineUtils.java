package org.springframework.kotlin.experimental.coroutine.util;

import kotlin.coroutines.experimental.Continuation;
import kotlin.coroutines.experimental.CoroutineContext;
import kotlin.jvm.functions.Function1;
import kotlinx.coroutines.experimental.BuildersKt;

public abstract class CoroutineUtils {
    private CoroutineUtils() {
    }

    static public <T> Object runCoroutine(final CoroutineContext context, final Function1<Continuation<? super T>, ? super T> fun, final Continuation<? super T> continuation) {
        return BuildersKt.run(context, fun, continuation);
    }
}
