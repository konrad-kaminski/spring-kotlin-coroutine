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

import kotlinx.coroutines.experimental.reactive.awaitFirstOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ClientResponse.Headers
import org.springframework.web.reactive.function.client.ExchangeStrategies

interface CoroutineClientResponse {

    fun statusCode(): HttpStatus

    fun headers(): Headers

    fun cookies(): MultiValueMap<String, ResponseCookie>

    fun strategies(): ExchangeStrategies

    suspend fun <T> body(elementClass: Class<T>): T?

    suspend fun <T> toEntity(bodyType: Class<T>): ResponseEntity<T?>

}

internal class DefaultCoroutineClientResponse(
    private val clientResponse: ClientResponse
) : CoroutineClientResponse {

    override fun statusCode(): HttpStatus = clientResponse.statusCode()

    override fun headers(): Headers = clientResponse.headers()

    override fun cookies(): MultiValueMap<String, ResponseCookie> = clientResponse.cookies()

    override fun strategies(): ExchangeStrategies = clientResponse.strategies()

    override suspend fun <T> body(elementClass: Class<T>): T? =
        clientResponse.bodyToMono(elementClass).awaitFirstOrNull()

    override suspend fun <T> toEntity(bodyType: Class<T>): ResponseEntity<T?> =
        ResponseEntity(clientResponse.bodyToMono(bodyType).awaitFirstOrNull(), headers().asHttpHeaders(), statusCode())
}

suspend inline fun <reified T : Any> CoroutineClientResponse.body(): T? = body(T::class.java)

suspend inline fun <reified T : Any> CoroutineClientResponse.toEntity(): ResponseEntity<T?> = toEntity(T::class.java)