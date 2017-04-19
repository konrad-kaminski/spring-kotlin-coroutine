package org.springframework.kotlin.experimental.coroutine.annotation

import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
annotation class Coroutine(
        val context: String = "",
        val name: String = ""
)