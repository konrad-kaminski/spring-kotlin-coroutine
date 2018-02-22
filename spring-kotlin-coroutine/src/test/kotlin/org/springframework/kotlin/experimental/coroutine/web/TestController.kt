package org.springframework.kotlin.experimental.coroutine.web

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
open class TestController {
    @GetMapping("/multiply/{a}/{b}")
    open fun multiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): Int = a*b

    @GetMapping("/notfound")
    open fun notfound() = ResponseEntity<Any?>(HttpStatus.NOT_FOUND)
}