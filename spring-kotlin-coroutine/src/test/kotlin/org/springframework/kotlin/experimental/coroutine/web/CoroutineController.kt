package org.springframework.kotlin.experimental.coroutine.web

import kotlinx.coroutines.experimental.delay
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
open class CoroutineController {
    @GetMapping("/delayedMultiply/{a}/{b}")
    suspend open fun delayedMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): Int {
        delay(1)

        return a*b
    }

    @GetMapping("/multiply/{a}/{b}")
    suspend open fun multiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): Int =
        a*b
}