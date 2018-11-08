/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.kotlin.experimental.coroutine.web

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import org.springframework.http.MediaType
import org.springframework.kotlin.experimental.coroutine.annotation.Coroutine
import org.springframework.kotlin.experimental.coroutine.context.DEFAULT_DISPATCHER
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
open class CoroutineController {
    @GetMapping("/suspendedMultiply/{a}/{b}")
    @Coroutine(DEFAULT_DISPATCHER)
    open suspend fun suspendedMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): Int =
        a*b

    @GetMapping("/multiply/{a}/{b}")
    open suspend fun multiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): Int =
        a*b

    @GetMapping("/delayedMultiply/{a}/{b}")
    open suspend fun delayedMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): Int {
        delay(1L)
        return a * b
    }

    @GetMapping("/channelMultiply/{a}/{b}")
    open fun channelMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int) = //: ReceiveChannel<Int>
        GlobalScope.produce {
            send(a*b)
        }

    @GetMapping("/delayedChannelMultiply/{a}/{b}")
    open suspend fun delayedChannelMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): ReceiveChannel<Int> {
        delay(1L)

        return GlobalScope.produce {
            send(a * b)
        }
    }

    @GetMapping("/channelMultiplyList/{a}/{b}")
    open suspend fun channelMultiplyList(@PathVariable("a") a: Int, @PathVariable("b") b: Int): List<Int> = listOf(a*b)

    @GetMapping("/suspendChannelMultiply/{a}/{b}")
    open suspend fun suspendChannelMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int) = //: ReceiveChannel<Int>
        GlobalScope.produce {
            send(a * b)
        }

    @GetMapping("/sseChannelMultiply/{a}/{b}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    open fun sseChannelMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int) = //: ReceiveChannel<Int>
            channelMultiply(a, b)

    @GetMapping("/sseDelayedChannelMultiply/{a}/{b}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    open suspend fun sseDelayedChannelMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): ReceiveChannel<Int> =
            delayedChannelMultiply(a, b)

    @GetMapping("/sseChannelMultiplyList/{a}/{b}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    open suspend fun sseChannelMultiplyList(@PathVariable("a") a: Int, @PathVariable("b") b: Int): List<Int> =
            channelMultiplyList(a, b)

    @GetMapping("/sseSuspendChannelMultiply/{a}/{b}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    open suspend fun sseSuspendChannelMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int) = //: ReceiveChannel<Int>
            suspendChannelMultiply(a, b)

    @GetMapping("/test", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    open fun test() = GlobalScope.produce(Dispatchers.Default) {
        repeat(5) {
            send(it)
            delay(500L)
        }
    }

    @GetMapping("/delayedTest", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    open suspend fun delayedTest() = GlobalScope.produce(Dispatchers.Default) {
        repeat(5) {
            send(it)
            delay(500L)
        }
    }

    @PostMapping("/postTest")
    open suspend fun postTest(): Int = 123456

    @PostMapping("/postWithHeaderTest")
    open suspend fun postWithHeaderTest(@RequestHeader("X-Coroutine-Test") headerValue: String): String = headerValue
}
