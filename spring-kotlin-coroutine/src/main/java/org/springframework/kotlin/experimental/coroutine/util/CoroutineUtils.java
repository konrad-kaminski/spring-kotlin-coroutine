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
