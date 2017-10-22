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

package org.springframework.web.coroutine.function.server

import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.reactor.mono
import kotlinx.coroutines.experimental.runBlocking
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicate
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.nest
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse

typealias CoroutineRoutes = suspend CoroutineRouterFunctionDsl.() -> Unit

typealias CoroutineHandlerFunction<T> = suspend (CoroutineServerRequest) -> T?

fun router(routes: CoroutineRoutes) = runBlocking {
    CoroutineRouterFunctionDsl().apply { routes(this) }.router()
}

class CoroutineRouterFunctionDsl {
    private val routes = mutableListOf<RouterFunction<ServerResponse>>()

    infix fun RequestPredicate.and(other: String): RequestPredicate = this.and(path(other))

    infix fun RequestPredicate.or(other: RequestPredicate): RequestPredicate = this.or(other)

    fun accept(mediaType: MediaType): RequestPredicate = RequestPredicates.accept(mediaType)

    fun contentType(mediaType: MediaType): RequestPredicate = RequestPredicates.contentType(mediaType)

    suspend fun RequestPredicate.nest(r: CoroutineRoutes) {
        routes += nest(this, CoroutineRouterFunctionDsl().apply { r(this) }.router())
    }

    suspend fun String.nest(r: CoroutineRoutes) = path(this).nest(r)

    fun DELETE(pattern: String): RequestPredicate = RequestPredicates.DELETE(pattern)

    fun GET(pattern: String) = RequestPredicates.GET(pattern)

    fun <T: CoroutineServerResponse> GET(pattern: String, f: CoroutineHandlerFunction<T>) {
        routes += route(RequestPredicates.GET(pattern), f.asHandlerFunction())
    }

    fun path(pattern: String): RequestPredicate = RequestPredicates.path(pattern)

    fun <T: CoroutineServerResponse> pathExtension(extension: String, f: CoroutineHandlerFunction<T>) {
        routes += route(RequestPredicates.pathExtension(extension), f.asHandlerFunction())
    }

    fun <T: CoroutineServerResponse> POST(pattern: String, f: CoroutineHandlerFunction<T>) {
        routes += route(RequestPredicates.POST(pattern), f.asHandlerFunction())
    }

    fun router(): RouterFunction<ServerResponse> {
        return routes.reduce(RouterFunction<ServerResponse>::and)
    }

    operator fun <T: CoroutineServerResponse> RequestPredicate.invoke(f: CoroutineHandlerFunction<T>) {
        routes += route(this, f.asHandlerFunction())
    }

    private fun <T: CoroutineServerResponse> CoroutineHandlerFunction<T>.asHandlerFunction() = HandlerFunction {
        mono(Unconfined) {
            this@asHandlerFunction.invoke(org.springframework.web.coroutine.function.server.CoroutineServerRequest(it))?.extractServerResponse()
        }
    }
}

operator fun <T: ServerResponse> RouterFunction<T>.plus(other: RouterFunction<T>) =
        this.and(other)