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

import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Deferred
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.kotlin.experimental.coroutine.NewThreadCoroutineDispatcher
import org.springframework.kotlin.experimental.coroutine.proxy.DeferredCoroutineProxyConfig
import org.springframework.kotlin.experimental.coroutine.proxy.createCoroutineProxy
import org.springframework.web.client.RequestCallback
import org.springframework.web.client.ResponseExtractor
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import java.net.URI
import kotlin.coroutines.experimental.CoroutineContext

interface DeferredRestOperations {
    fun <T : Any?> postForObject(url: String, request: Any?, responseType: Class<T>?, vararg uriVariables: Any?): Deferred<T>

    fun <T : Any?> postForObject(url: String, request: Any?, responseType: Class<T>?, uriVariables: Map<String, *>): Deferred<T>

    fun <T : Any?> postForObject(url: URI, request: Any?, responseType: Class<T>?): Deferred<T>

    fun put(url: String, request: Any?, vararg uriVariables: Any?): Deferred<Unit>

    fun put(url: String, request: Any?, uriVariables: Map<String, *>): Deferred<Unit>

    fun put(url: URI, request: Any?): Deferred<Unit>

    fun <T : Any?> exchange(url: String, method: HttpMethod, requestEntity: HttpEntity<*>?, responseType: Class<T>?, vararg uriVariables: Any?): Deferred<ResponseEntity<T>>

    fun <T : Any?> exchange(url: String, method: HttpMethod, requestEntity: HttpEntity<*>?, responseType: Class<T>?, uriVariables: Map<String, *>): Deferred<ResponseEntity<T>>

    fun <T : Any?> exchange(url: URI, method: HttpMethod, requestEntity: HttpEntity<*>?, responseType: Class<T>?): Deferred<ResponseEntity<T>>

    fun <T : Any?> exchange(url: String, method: HttpMethod, requestEntity: HttpEntity<*>?, responseType: ParameterizedTypeReference<T>, vararg uriVariables: Any?): Deferred<ResponseEntity<T>>

    fun <T : Any?> exchange(url: String, method: HttpMethod, requestEntity: HttpEntity<*>?, responseType: ParameterizedTypeReference<T>, uriVariables: Map<String, *>): Deferred<ResponseEntity<T>>

    fun <T : Any?> exchange(url: URI, method: HttpMethod, requestEntity: HttpEntity<*>?, responseType: ParameterizedTypeReference<T>): Deferred<ResponseEntity<T>>

    fun <T : Any?> exchange(requestEntity: RequestEntity<*>?, responseType: Class<T>?): Deferred<ResponseEntity<T>>

    fun <T : Any?> exchange(requestEntity: RequestEntity<*>?, responseType: ParameterizedTypeReference<T>): Deferred<ResponseEntity<T>>

    fun <T : Any?> getForEntity(url: String, responseType: Class<T>?, vararg uriVariables: Any?): Deferred<ResponseEntity<T>>

    fun <T : Any?> getForEntity(url: String, responseType: Class<T>?, uriVariables: Map<String, *>): Deferred<ResponseEntity<T>>

    fun <T : Any?> getForEntity(url: URI, responseType: Class<T>?): Deferred<ResponseEntity<T>>

    fun headForHeaders(url: String, vararg uriVariables: Any?): Deferred<HttpHeaders>

    fun headForHeaders(url: String, uriVariables: Map<String, *>): Deferred<HttpHeaders>

    fun headForHeaders(url: URI): Deferred<HttpHeaders>

    fun <T : Any?> getForObject(url: String, responseType: Class<T>?, vararg uriVariables: Any?): Deferred<T>

    fun <T : Any?> getForObject(url: String, responseType: Class<T>?, uriVariables: Map<String, *>): Deferred<T>

    fun <T : Any?> getForObject(url: URI, responseType: Class<T>?): Deferred<T>

    fun <T : Any?> execute(url: String, method: HttpMethod, requestCallback: RequestCallback?, responseExtractor: ResponseExtractor<T>?, vararg uriVariables: Any?): Deferred<T>

    fun <T : Any?> execute(url: String, method: HttpMethod, requestCallback: RequestCallback?, responseExtractor: ResponseExtractor<T>?, uriVariables: Map<String, *>): Deferred<T>

    fun <T : Any?> execute(url: URI, method: HttpMethod, requestCallback: RequestCallback?, responseExtractor: ResponseExtractor<T>?): Deferred<T>

    fun <T : Any?> postForEntity(url: String, request: Any?, responseType: Class<T>?, vararg uriVariables: Any?): Deferred<ResponseEntity<T>>

    fun <T : Any?> postForEntity(url: String, request: Any?, responseType: Class<T>?, uriVariables: Map<String, *>): Deferred<ResponseEntity<T>>

    fun <T : Any?> postForEntity(url: URI, request: Any?, responseType: Class<T>?): Deferred<ResponseEntity<T>>

    fun optionsForAllow(url: String, vararg uriVariables: Any?): Deferred<Set<HttpMethod>>

    fun optionsForAllow(url: String, uriVariables: Map<String, *>): Deferred<Set<HttpMethod>>

    fun optionsForAllow(url: URI): Deferred<Set<HttpMethod>>

    fun <T : Any?> patchForObject(url: String, request: Any?, responseType: Class<T>?, vararg uriVariables: Any?): Deferred<T>

    fun <T : Any?> patchForObject(url: String, request: Any?, responseType: Class<T>?, uriVariables: Map<String, *>): Deferred<T>

    fun <T : Any?> patchForObject(url: URI, request: Any?, responseType: Class<T>?): Deferred<T>

    fun postForLocation(url: String, request: Any?, vararg uriVariables: Any?): Deferred<URI>

    fun postForLocation(url: String, request: Any?, uriVariables: Map<String, *>): Deferred<URI>

    fun postForLocation(url: URI, request: Any?): Deferred<URI>

    fun delete(url: String, vararg uriVariables: Any?): Deferred<Unit>

    fun delete(url: String, uriVariables: Map<String, *>): Deferred<Unit>

    fun delete(url: URI): Deferred<Unit>

    companion object {
        operator fun invoke(restOperations: RestOperations = RestTemplate(), start: CoroutineStart = CoroutineStart.DEFAULT,
                            context: CoroutineContext = NewThreadCoroutineDispatcher): DeferredRestOperations =
                createCoroutineProxy(DeferredRestOperations::class.java, restOperations, DeferredCoroutineProxyConfig(start, context))
    }
}