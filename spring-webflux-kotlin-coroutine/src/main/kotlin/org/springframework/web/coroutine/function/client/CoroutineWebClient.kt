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

import org.springframework.http.HttpMethod
import org.springframework.kotlin.experimental.coroutine.web.awaitFirstOrNull
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI

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
    }

    interface RequestHeadersUriSpec<T: RequestHeadersSpec<T>>: UriSpec<T>, RequestHeadersSpec<T> {
    }

    interface UriSpec<T: RequestHeadersSpec<T>> {
        fun uri(uri: URI): T
    }

    interface RequestHeadersSpec<T: RequestHeadersSpec<T>> {
        suspend fun exchange(): CoroutineClientResponse?

        suspend fun retrieve(): CoroutineResponseSpec
    }

    interface CoroutineResponseSpec {
        suspend fun <T> body(clazz: Class<T>): T?
    }

    companion object {
        fun create(): CoroutineWebClient = DefaultCoroutineWebClient(WebClient.create())
        fun create(url: String): CoroutineWebClient = DefaultCoroutineWebClient(WebClient.create(url))
    }
}


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
    suspend override fun <T> body(clazz: Class<T>): T? =
            spec.bodyToMono(clazz).awaitFirstOrNull()
}

open class DefaultRequestBodyUriSpec(
    private val spec: WebClient.RequestBodyUriSpec
): CoroutineWebClient.RequestBodyUriSpec {
    override fun uri(uri: URI): CoroutineWebClient.RequestBodySpec = apply {
        spec.uri(uri)
    }

    suspend override fun exchange(): CoroutineClientResponse? = spec.exchange().awaitFirstOrNull()?.let {
        DefaultCoroutineClientResponse(it)
    }

    suspend override fun retrieve(): CoroutineWebClient.CoroutineResponseSpec =
        spec.retrieve().asCoroutineResponseSpec()
}

