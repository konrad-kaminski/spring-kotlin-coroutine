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

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.delay
import org.springframework.http.MediaType
import org.springframework.kotlin.experimental.coroutine.annotation.Coroutine
import org.springframework.kotlin.experimental.coroutine.context.COMMON_POOL
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
open class CoroutineController {
    @GetMapping("/suspendedMultiply/{a}/{b}")
    @Coroutine(COMMON_POOL)
    suspend open fun suspendedMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): Int =
        a*b

    @GetMapping("/multiply/{a}/{b}")
    suspend open fun multiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): Int =
        a*b

    @GetMapping("/delayedMultiply/{a}/{b}")
    suspend open fun delayedMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): Int {
        delay(1L)
        return a * b
    }

    @GetMapping("/channelMultiply/{a}/{b}")
    open fun channelMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int) = //: ReceiveChannel<Int>
        produce {
            send(a*b)
        }

    @GetMapping("/delayedChannelMultiply/{a}/{b}")
    suspend open fun delayedChannelMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): ReceiveChannel<Int> {
        delay(1L)

        return produce {
            send(a * b)
        }
    }

    @GetMapping("/channelMultiplyList/{a}/{b}")
    suspend open fun channelMultiplyList(@PathVariable("a") a: Int, @PathVariable("b") b: Int): List<Int> = listOf(a*b)

    @GetMapping("/suspendChannelMultiply/{a}/{b}")
    suspend open fun suspendChannelMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int) = //: ReceiveChannel<Int>
        produce {
            send(a * b)
        }

    @GetMapping("/sseChannelMultiply/{a}/{b}", produces = arrayOf(MediaType.TEXT_EVENT_STREAM_VALUE))
    open fun sseChannelMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int) = //: ReceiveChannel<Int>
            channelMultiply(a, b)

    @GetMapping("/sseDelayedChannelMultiply/{a}/{b}", produces = arrayOf(MediaType.TEXT_EVENT_STREAM_VALUE))
    suspend open fun sseDelayedChannelMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): ReceiveChannel<Int> =
            delayedChannelMultiply(a, b)

    @GetMapping("/sseChannelMultiplyList/{a}/{b}", produces = arrayOf(MediaType.TEXT_EVENT_STREAM_VALUE))
    suspend open fun sseChannelMultiplyList(@PathVariable("a") a: Int, @PathVariable("b") b: Int): List<Int> =
            channelMultiplyList(a, b)

    @GetMapping("/sseSuspendChannelMultiply/{a}/{b}", produces = arrayOf(MediaType.TEXT_EVENT_STREAM_VALUE))
    suspend open fun sseSuspendChannelMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int) = //: ReceiveChannel<Int>
            suspendChannelMultiply(a, b)

    @GetMapping("/test", produces = arrayOf(MediaType.TEXT_EVENT_STREAM_VALUE))
    open fun test() = produce(CommonPool) {
        repeat(5) {
            send(it)
            delay(500L)
        }
    }

    @GetMapping("/delayedTest", produces = arrayOf(MediaType.TEXT_EVENT_STREAM_VALUE))
    suspend open fun delayedTest() = produce(CommonPool) {
        repeat(5) {
            send(it)
            delay(500L)
        }
    }

    @PostMapping("/postTest")
    suspend open fun postTest(): Int = 123456
}