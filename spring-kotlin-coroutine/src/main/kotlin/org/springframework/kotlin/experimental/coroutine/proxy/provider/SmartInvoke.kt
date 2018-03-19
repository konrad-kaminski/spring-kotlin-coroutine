package org.springframework.kotlin.experimental.coroutine.proxy.provider

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

internal fun Method.smartInvoke(obj: Any, vararg args: Any): Any =
    try {
        invoke(obj, *args)
    } catch (ex: InvocationTargetException) {
        throw ex.targetException
    }
