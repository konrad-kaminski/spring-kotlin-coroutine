package org.springframework.kotlin.experimental.coroutine.web

import kotlinx.coroutines.experimental.delay
import org.springframework.kotlin.experimental.coroutine.annotation.Coroutine
import org.springframework.kotlin.experimental.coroutine.context.COMMON_POOL
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
}