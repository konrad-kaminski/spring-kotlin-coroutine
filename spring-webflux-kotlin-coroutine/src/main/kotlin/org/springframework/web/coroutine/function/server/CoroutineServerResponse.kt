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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import org.springframework.http.server.CoroutineServerHttpResponse
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.kotlin.experimental.coroutine.web.awaitFirstOrNull
import org.springframework.util.MultiValueMap
import org.springframework.web.coroutine.function.CoroutineBodyInserter
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.server.RenderingResponse
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI
import java.time.ZonedDateTime

interface CoroutineServerResponse {
    fun extractServerResponse(): ServerResponse

    companion object {
        operator fun invoke(resp: ServerResponse): CoroutineServerResponse = DefaultCoroutineServerResponse(resp)

        fun accepted(): CoroutineBodyBuilder = ServerResponse.accepted().asCoroutineBodyBuilder()

        fun badRequest(): CoroutineBodyBuilder = ServerResponse.badRequest().asCoroutineBodyBuilder()

        fun created(location: URI): CoroutineBodyBuilder = ServerResponse.created(location).asCoroutineBodyBuilder()

        fun from(other: CoroutineServerResponse): CoroutineBodyBuilder =
                ServerResponse.from(other.extractServerResponse()).asCoroutineBodyBuilder()

        fun noContent(): CoroutineHeadersBuilder =
                (ServerResponse.noContent() as ServerResponse.BodyBuilder).asCoroutineBodyBuilder()

        fun notFound(): CoroutineHeadersBuilder =
                (ServerResponse.notFound() as ServerResponse.BodyBuilder).asCoroutineBodyBuilder()

        fun ok(): CoroutineBodyBuilder = ServerResponse.ok().asCoroutineBodyBuilder()

        fun permanentRedirect(location: URI): CoroutineBodyBuilder =
                ServerResponse.permanentRedirect(location).asCoroutineBodyBuilder()

        fun seeOther(location: URI): CoroutineBodyBuilder = ServerResponse.seeOther(location).asCoroutineBodyBuilder()

        fun status(status: Int): CoroutineBodyBuilder = ServerResponse.status(status).asCoroutineBodyBuilder()

        fun status(status: HttpStatus): CoroutineBodyBuilder = ServerResponse.status(status).asCoroutineBodyBuilder()

        fun temporaryRedirect(location: URI): CoroutineBodyBuilder =
                ServerResponse.temporaryRedirect(location).asCoroutineBodyBuilder()

        fun unprocessableEntity(): CoroutineBodyBuilder =
                ServerResponse.unprocessableEntity().asCoroutineBodyBuilder()
    }
}

interface CoroutineHeadersBuilder {
    fun allow(vararg allowedMethods: HttpMethod): CoroutineHeadersBuilder

    fun allow(allowedMethods: Set<HttpMethod>): CoroutineHeadersBuilder

    fun cacheControl(cacheControl: CacheControl): CoroutineHeadersBuilder

    fun cookie(cookie: ResponseCookie): CoroutineHeadersBuilder

    fun cookies(cookiesConsumer: (MultiValueMap<String, ResponseCookie>) -> Unit): CoroutineHeadersBuilder

    fun eTag(eTag: String): CoroutineHeadersBuilder

    fun header(headerName: String, vararg headerValues: String): CoroutineHeadersBuilder

    fun headers(headersConsumer: (HttpHeaders) -> Unit): CoroutineHeadersBuilder

    fun lastModified(lastModified: ZonedDateTime): CoroutineHeadersBuilder

    fun location(location: URI): CoroutineHeadersBuilder

    fun varyBy(vararg requestHeaders: String): CoroutineHeadersBuilder
}

interface CoroutineBodyBuilder: CoroutineHeadersBuilder {
    suspend fun build(): CoroutineServerResponse?

    suspend fun body(inserter: CoroutineBodyInserter<*, in CoroutineServerHttpResponse>): CoroutineServerResponse?

    suspend fun <T> body(value: T?, elementClass: Class<T>): CoroutineServerResponse?

    suspend fun <T> body(channel: ReceiveChannel<T>, elementClass: Class<T>): CoroutineServerResponse?

    fun contentType(contentType: MediaType): CoroutineBodyBuilder

    suspend fun render(name: String, vararg modelAttributes: Any): CoroutineServerResponse?

    suspend fun render(name: String, model: Map<String, *>): CoroutineServerResponse?

    suspend fun syncBody(body: Any): CoroutineServerResponse?

    companion object {
        operator fun invoke(builder: ServerResponse.BodyBuilder): CoroutineBodyBuilder = DefaultCoroutineBodyBuilder(builder)
    }
}

internal open class DefaultCoroutineServerResponse(val resp: ServerResponse): CoroutineServerResponse {
    override fun extractServerResponse(): ServerResponse = resp
}

internal open class DefaultCoroutineHeadersBuilder<T: ServerResponse.HeadersBuilder<T>>(var builder: T): CoroutineHeadersBuilder {
    override fun allow(vararg allowedMethods: HttpMethod): CoroutineHeadersBuilder = apply {
        builder.allow(*allowedMethods)
    }

    override fun allow(allowedMethods: Set<HttpMethod>): CoroutineHeadersBuilder = apply {
        builder.allow(allowedMethods)
    }

    override fun cacheControl(cacheControl: CacheControl): CoroutineHeadersBuilder = apply {
        builder.cacheControl(cacheControl)
    }

    override fun cookie(cookie: ResponseCookie): CoroutineHeadersBuilder = apply {
        builder.cookie(cookie)
    }

    override fun cookies(cookiesConsumer: (MultiValueMap<String, ResponseCookie>) -> Unit): CoroutineHeadersBuilder = apply {
        builder.cookies(cookiesConsumer)
    }

    override fun eTag(eTag: String): CoroutineHeadersBuilder = apply {
        builder.eTag(eTag)
    }

    override fun header(headerName: String, vararg headerValues: String): CoroutineHeadersBuilder = apply {
        builder.header(headerName, *headerValues)
    }

    override fun headers(headersConsumer: (HttpHeaders) -> Unit): CoroutineHeadersBuilder = apply {
        builder.headers(headersConsumer)
    }

    override fun lastModified(lastModified: ZonedDateTime): CoroutineHeadersBuilder = apply {
        builder.lastModified(lastModified)
    }

    override fun location(location: URI): CoroutineHeadersBuilder = apply {
        builder.location(location)
    }

    override fun varyBy(vararg requestHeaders: String): CoroutineHeadersBuilder = apply {
        builder.varyBy(*requestHeaders)
    }
}

internal open class DefaultCoroutineBodyBuilder(builder: ServerResponse.BodyBuilder): DefaultCoroutineHeadersBuilder<ServerResponse.BodyBuilder>(builder), CoroutineBodyBuilder {
    override suspend fun build(): CoroutineServerResponse? = builder.build().asCoroutineServerResponse()

    override suspend fun body(inserter: CoroutineBodyInserter<*, in CoroutineServerHttpResponse>): CoroutineServerResponse? =
            builder.body(inserter.asBodyInserter()).asCoroutineServerResponse()

    override suspend fun <T> body(value: T?, elementClass: Class<T>): CoroutineServerResponse? =
            builder.body(Mono.justOrEmpty(value), elementClass as Class<T?>).asCoroutineServerResponse()

    override suspend fun <T> body(channel: ReceiveChannel<T>, elementClass: Class<T>): CoroutineServerResponse? =
            builder.body(channel.asPublisher(Dispatchers.Unconfined), elementClass).asCoroutineServerResponse()

    override fun contentType(contentType: MediaType): CoroutineBodyBuilder = apply {
        builder.contentType(contentType)
    }

    override suspend fun render(name: String, vararg modelAttributes: Any): CoroutineServerResponse? =
            builder.render(name, modelAttributes).awaitFirstOrNull()?.asCoroutineServerResponse()

    override suspend fun render(name: String, model: Map<String, *>): CoroutineServerResponse? =
            builder.render(name, model).awaitFirstOrNull()?.asCoroutineServerResponse()

    override suspend fun syncBody(body: Any): CoroutineServerResponse? =
            builder.syncBody(body).asCoroutineServerResponse()

    suspend fun Mono<ServerResponse>.asCoroutineServerResponse(): CoroutineServerResponse? =
            awaitFirstOrNull()?.let { CoroutineServerResponse(it) }
}

internal open class DefaultCoroutineRenderingResponse(resp: RenderingResponse): DefaultCoroutineServerResponse(resp), CoroutineRenderingResponse {
}

class DefaultCoroutineRenderingResponseBuilder(val builder: RenderingResponse.Builder): CoroutineRenderingResponse.Builder {
    override suspend fun build(): CoroutineRenderingResponse =
            CoroutineRenderingResponse(builder.build().awaitFirst())

    override fun modelAttributes(attributes: Map<String, *>): CoroutineRenderingResponse.Builder = apply {
        builder.modelAttributes(attributes)
    }
}

private fun CoroutineBodyInserter<*, in CoroutineServerHttpResponse>.asBodyInserter(): BodyInserter<Any, ServerHttpResponse> = TODO()

private fun ServerResponse.BodyBuilder.asCoroutineBodyBuilder(): CoroutineBodyBuilder = CoroutineBodyBuilder(this)

inline suspend fun <reified T : Any> CoroutineBodyBuilder.body(channel: ReceiveChannel<T>): CoroutineServerResponse? =
        body(channel, T::class.java)

inline suspend fun <reified T: Any> CoroutineBodyBuilder.body(value: T?): CoroutineServerResponse? =
        body(value, T::class.java)

inline fun <T : CoroutineServerResponse> ServerResponse.asCoroutineServerResponse(): T =
        when (this) {
            is RenderingResponse -> CoroutineRenderingResponse(this)
            else                 -> CoroutineServerResponse(this)
        } as T
