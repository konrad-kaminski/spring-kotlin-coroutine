/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.kotlin.experimental.coroutine.web.client

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.kotlin.experimental.coroutine.NewThreadCoroutineDispatcher
import org.springframework.kotlin.experimental.coroutine.proxy.DefaultCoroutineProxyConfig
import org.springframework.kotlin.experimental.coroutine.proxy.createCoroutineProxy
import org.springframework.web.client.RequestCallback
import org.springframework.web.client.ResponseExtractor
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import java.net.URI
import kotlin.coroutines.CoroutineContext

interface CoroutineRestOperations {
    suspend fun <T : Any?> postForObject(url: String, request: Any?, responseType: Class<T>?, vararg uriVariables: Any?): T

    suspend fun <T : Any?> postForObject(url: String, request: Any?, responseType: Class<T>?, uriVariables: Map<String, *>): T

    suspend fun <T : Any?> postForObject(url: URI, request: Any?, responseType: Class<T>?): T

    suspend fun put(url: String, request: Any?, vararg uriVariables: Any?)

    suspend fun put(url: String, request: Any?, uriVariables: Map<String, *>)

    suspend fun put(url: URI, request: Any?)

    suspend fun <T : Any?> exchange(url: String, method: HttpMethod, requestEntity: HttpEntity<*>?, responseType: Class<T>?, vararg uriVariables: Any?): ResponseEntity<T>

    suspend fun <T : Any?> exchange(url: String, method: HttpMethod, requestEntity: HttpEntity<*>?, responseType: Class<T>?, uriVariables: Map<String, *>): ResponseEntity<T>

    suspend fun <T : Any?> exchange(url: URI, method: HttpMethod, requestEntity: HttpEntity<*>?, responseType: Class<T>?): ResponseEntity<T>

    suspend fun <T : Any?> exchange(url: String, method: HttpMethod, requestEntity: HttpEntity<*>?, responseType: ParameterizedTypeReference<T>, vararg uriVariables: Any?): ResponseEntity<T>

    suspend fun <T : Any?> exchange(url: String, method: HttpMethod, requestEntity: HttpEntity<*>?, responseType: ParameterizedTypeReference<T>, uriVariables: Map<String, *>): ResponseEntity<T>

    suspend fun <T : Any?> exchange(url: URI, method: HttpMethod, requestEntity: HttpEntity<*>?, responseType: ParameterizedTypeReference<T>): ResponseEntity<T>

    suspend fun <T : Any?> exchange(requestEntity: RequestEntity<*>?, responseType: Class<T>?): ResponseEntity<T>

    suspend fun <T : Any?> exchange(requestEntity: RequestEntity<*>?, responseType: ParameterizedTypeReference<T>): ResponseEntity<T>

    suspend fun <T : Any?> getForEntity(url: String, responseType: Class<T>?, vararg uriVariables: Any?): ResponseEntity<T>

    suspend fun <T : Any?> getForEntity(url: String, responseType: Class<T>?, uriVariables: Map<String, *>): ResponseEntity<T>

    suspend fun <T : Any?> getForEntity(url: URI, responseType: Class<T>?): ResponseEntity<T>

    suspend fun headForHeaders(url: String, vararg uriVariables: Any?): HttpHeaders

    suspend fun headForHeaders(url: String, uriVariables: Map<String, *>): HttpHeaders

    suspend fun headForHeaders(url: URI): HttpHeaders

    suspend fun <T : Any?> getForObject(url: String, responseType: Class<T>?, vararg uriVariables: Any?): T

    suspend fun <T : Any?> getForObject(url: String, responseType: Class<T>?, uriVariables: Map<String, *>): T

    suspend fun <T : Any?> getForObject(url: URI, responseType: Class<T>?): T

    suspend fun <T : Any?> execute(url: String, method: HttpMethod, requestCallback: RequestCallback?, responseExtractor: ResponseExtractor<T>?, vararg uriVariables: Any?): T

    suspend fun <T : Any?> execute(url: String, method: HttpMethod, requestCallback: RequestCallback?, responseExtractor: ResponseExtractor<T>?, uriVariables: Map<String, *>): T

    suspend fun <T : Any?> execute(url: URI, method: HttpMethod, requestCallback: RequestCallback?, responseExtractor: ResponseExtractor<T>?): T

    suspend fun <T : Any?> postForEntity(url: String, request: Any?, responseType: Class<T>?, vararg uriVariables: Any?): ResponseEntity<T>

    suspend fun <T : Any?> postForEntity(url: String, request: Any?, responseType: Class<T>?, uriVariables: Map<String, *>): ResponseEntity<T>

    suspend fun <T : Any?> postForEntity(url: URI, request: Any?, responseType: Class<T>?): ResponseEntity<T>

    suspend fun optionsForAllow(url: String, vararg uriVariables: Any?): Set<HttpMethod>

    suspend fun optionsForAllow(url: String, uriVariables: Map<String, *>): Set<HttpMethod>

    suspend fun optionsForAllow(url: URI): Set<HttpMethod>

    suspend fun <T : Any?> patchForObject(url: String, request: Any?, responseType: Class<T>?, vararg uriVariables: Any?): T

    suspend fun <T : Any?> patchForObject(url: String, request: Any?, responseType: Class<T>?, uriVariables: Map<String, *>): T

    suspend fun <T : Any?> patchForObject(url: URI, request: Any?, responseType: Class<T>?): T

    suspend fun postForLocation(url: String, request: Any?, vararg uriVariables: Any?): URI

    suspend fun postForLocation(url: String, request: Any?, uriVariables: Map<String, *>): URI

    suspend fun postForLocation(url: URI, request: Any?): URI

    suspend fun delete(url: String, vararg uriVariables: Any?)

    suspend fun delete(url: String, uriVariables: Map<String, *>)

    suspend fun delete(url: URI)

    companion object {
        operator fun invoke(restOperations: RestOperations = RestTemplate(),
            context: CoroutineContext? = NewThreadCoroutineDispatcher): CoroutineRestOperations =
                createCoroutineProxy(CoroutineRestOperations::class.java, restOperations, DefaultCoroutineProxyConfig(context))
    }
}
