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

package org.springframework.web.coroutine.function.client

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.kotlin.experimental.coroutine.web.awaitFirstOrNull
import org.springframework.util.MultiValueMap
import org.springframework.web.coroutine.function.client.CoroutineWebClient.CoroutineResponseSpec
import org.springframework.web.coroutine.function.client.CoroutineWebClient.RequestBodySpec
import org.springframework.web.coroutine.function.client.CoroutineWebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI
import java.nio.charset.Charset
import java.time.ZonedDateTime

interface CoroutineWebClient {
    fun get(): CoroutineWebClient.RequestHeadersUriSpec<*>

    fun head(): CoroutineWebClient.RequestHeadersUriSpec<*>

    fun post(): CoroutineWebClient.RequestBodyUriSpec

    fun put(): CoroutineWebClient.RequestBodyUriSpec

    fun patch(): CoroutineWebClient.RequestBodyUriSpec

    fun delete(): CoroutineWebClient.RequestHeadersUriSpec<*>

    fun options(): CoroutineWebClient.RequestHeadersUriSpec<*>

    fun method(method: HttpMethod): CoroutineWebClient.RequestBodyUriSpec

    interface RequestBodyUriSpec: RequestBodySpec, RequestHeadersUriSpec<RequestBodySpec>

    interface RequestBodySpec: RequestHeadersSpec<RequestBodySpec>  {

        fun contentLength(contentLength: Long): RequestBodySpec

        fun contentType(contentType: MediaType): RequestBodySpec

        fun <T> body(body: T, elementClass: Class<T>): RequestHeadersSpec<*>

        fun syncBody(body: Any): RequestHeadersSpec<*>

    }

    interface RequestHeadersUriSpec<T: RequestHeadersSpec<T>>: UriSpec<T>, RequestHeadersSpec<T> {
    }

    interface UriSpec<T: RequestHeadersSpec<T>> {
        fun uri(uri: String, vararg uriVariables: Any): T

        fun uri(uri: String, uriVariables: Map<String, *>): T

        fun uri(uri: URI): T
    }

    interface RequestHeadersSpec<T: RequestHeadersSpec<T>> {
        fun accept(vararg acceptableMediaTypes: MediaType): T

        fun acceptCharset(vararg acceptableCharsets: Charset): T

        fun cookie(name: String, value: String): T

        fun cookies(cookiesConsumer: (MultiValueMap<String, String>) -> Unit): T

        fun ifModifiedSince(ifModifiedSince: ZonedDateTime): T

        fun ifNoneMatch(vararg ifNoneMatches: String): T

        fun header(headerName: String, vararg headerValues: String): T

        fun headers(headersConsumer: (HttpHeaders) -> Unit): T

        fun attribute(name: String, value: Any): T

        fun attributes(attributesConsumer: (Map<String, Any>) -> Unit): T

        fun retrieve(): CoroutineResponseSpec

        suspend fun exchange(): CoroutineClientResponse?
    }

    interface CoroutineResponseSpec {
        suspend fun <T> body(clazz: Class<T>): T?

        fun onStatus(
            statusPredicate: (HttpStatus) -> Boolean,
            exceptionFunction: (ClientResponse) -> Mono<out Throwable>
        ): CoroutineResponseSpec
    }

    companion object {
        fun create(): CoroutineWebClient = DefaultCoroutineWebClient(WebClient.create())
        fun create(url: String): CoroutineWebClient = DefaultCoroutineWebClient(WebClient.create(url))
    }
}

suspend inline fun <reified T : Any> CoroutineWebClient.CoroutineResponseSpec.body(): T? = body(T::class.java)

inline fun <reified T : Any> CoroutineWebClient.RequestBodySpec.body(body: T): RequestHeadersSpec<*> = body(body, T::class.java)

open class DefaultCoroutineWebClient(
    private val client: WebClient
) : CoroutineWebClient {
    override fun get(): CoroutineWebClient.RequestHeadersUriSpec<*> = request { client.get() }

    override fun head(): CoroutineWebClient.RequestHeadersUriSpec<*> = request { client.head() }

    override fun post(): CoroutineWebClient.RequestBodyUriSpec = request { client.post() }

    override fun put(): CoroutineWebClient.RequestBodyUriSpec = request { client.put() }

    override fun patch(): CoroutineWebClient.RequestBodyUriSpec = request { client.patch() }

    override fun delete(): CoroutineWebClient.RequestHeadersUriSpec<*> = request { client.delete() }

    override fun options(): CoroutineWebClient.RequestHeadersUriSpec<*> = request { client.options() }

    override fun method(method: HttpMethod): CoroutineWebClient.RequestBodyUriSpec = request { client.method(method) }

    private fun request(f: () -> WebClient.RequestHeadersUriSpec<*>): CoroutineWebClient.RequestBodyUriSpec =
        DefaultRequestBodyUriSpec(f.invoke() as WebClient.RequestBodyUriSpec)
}

private fun WebClient.ResponseSpec.asCoroutineResponseSpec(): CoroutineWebClient.CoroutineResponseSpec =
    DefaultCoroutineResponseSpec(this)

open class DefaultCoroutineResponseSpec(
    private val spec: WebClient.ResponseSpec
): CoroutineWebClient.CoroutineResponseSpec {

    override suspend fun <T> body(clazz: Class<T>): T? = spec.bodyToMono(clazz).awaitFirstOrNull()

    override fun onStatus(
        statusPredicate: (HttpStatus) -> Boolean,
        exceptionFunction: (ClientResponse) -> Mono<out Throwable>
    ): CoroutineResponseSpec = apply {
        spec.onStatus(statusPredicate, exceptionFunction)
    }
}

open class DefaultRequestBodyUriSpec(
    private val spec: WebClient.RequestBodyUriSpec
): CoroutineWebClient.RequestBodyUriSpec {

    override fun uri(uri: String, vararg uriVariables: Any): CoroutineWebClient.RequestBodySpec = apply {
        spec.uri(uri, *uriVariables)
    }

    override fun uri(uri: String, uriVariables: Map<String, *>): CoroutineWebClient.RequestBodySpec = apply {
        spec.uri(uri, uriVariables)
    }

    override fun uri(uri: URI): CoroutineWebClient.RequestBodySpec = apply {
        spec.uri(uri)
    }

    override fun accept(vararg acceptableMediaTypes: MediaType): CoroutineWebClient.RequestBodySpec = apply {
        spec.accept(*acceptableMediaTypes)
    }

    override fun acceptCharset(vararg acceptableCharsets: Charset): CoroutineWebClient.RequestBodySpec = apply {
        spec.acceptCharset(*acceptableCharsets)
    }

    override fun cookie(name: String, value: String): CoroutineWebClient.RequestBodySpec = apply {
        spec.cookie(name, value)
    }

    override fun cookies(cookiesConsumer: (MultiValueMap<String, String>) -> Unit): CoroutineWebClient.RequestBodySpec = apply {
        spec.cookies(cookiesConsumer)
    }

    override fun ifModifiedSince(ifModifiedSince: ZonedDateTime): CoroutineWebClient.RequestBodySpec = apply {
        spec.ifModifiedSince(ifModifiedSince)
    }

    override fun ifNoneMatch(vararg ifNoneMatches: String): CoroutineWebClient.RequestBodySpec = apply {
        spec.ifNoneMatch(*ifNoneMatches)
    }

    override fun header(headerName: String, vararg headerValues: String): CoroutineWebClient.RequestBodySpec = apply {
        spec.header(headerName, *headerValues)
    }

    override fun headers(headersConsumer: (HttpHeaders) -> Unit): CoroutineWebClient.RequestBodySpec = apply {
        spec.headers(headersConsumer)
    }

    override fun attribute(name: String, value: Any): CoroutineWebClient.RequestBodySpec = apply {
        spec.attribute(name, value)
    }

    override fun attributes(attributesConsumer: (Map<String, Any>) -> Unit): CoroutineWebClient.RequestBodySpec = apply {
        spec.attributes(attributesConsumer)
    }

    override suspend fun exchange(): CoroutineClientResponse? = spec.exchange().awaitFirstOrNull()?.let {
        DefaultCoroutineClientResponse(it)
    }

    override fun retrieve(): CoroutineWebClient.CoroutineResponseSpec =
        spec.retrieve().asCoroutineResponseSpec()

    override fun contentLength(contentLength: Long): RequestBodySpec = apply {
        spec.contentLength(contentLength)
    }

    override fun contentType(contentType: MediaType): RequestBodySpec = apply {
        spec.contentType(contentType)
    }

    override fun <T> body(body: T, elementClass: Class<T>): RequestHeadersSpec<*> = apply {
        spec.body(Mono.justOrEmpty(body), elementClass)
    }

    override fun syncBody(body: Any): RequestHeadersSpec<*> = apply {
        spec.syncBody(body)
    }
}