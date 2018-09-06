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

package org.springframework.kotlin.experimental.coroutine.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.coroutine.function.server.CoroutineServerRequest
import org.springframework.web.coroutine.function.server.CoroutineServerResponse
import org.springframework.web.coroutine.function.server.body
import org.springframework.web.coroutine.function.server.router

@Configuration
open class FunctionalStyleRoutesConfiguration {
    @Bean
    open fun functionalHandler() = FunctionalHandler()

    @Bean
    open fun apiRouter(handler: FunctionalHandler) = router {
        "/test-functional".nest {
            (accept(MediaType.APPLICATION_JSON) and  "/").nest {
                GET("/simple/{param}") { handler.simpleGet(it) }
            }

            ("/" and contentType(MediaType.APPLICATION_JSON)).nest {
                POST("/simple") { handler.simplePost(it) }
                PUT("/simple/{id}") { handler.simplePut(it) }
            }

            DELETE("/simple/{id}") { handler.simpleDelete(it) }
        }
    }
}

@Component
open class FunctionalHandler {
    suspend fun simpleGet(req: CoroutineServerRequest) =
        CoroutineServerResponse.ok().contentType(MediaType.TEXT_PLAIN).body(req.pathVariable("param"))

    suspend fun simplePost(req: CoroutineServerRequest) =
        CoroutineServerResponse.created(req.uri()).body(mapOf(req.body<Map<String, String>>()!!["key"] to "pong"))

    suspend fun simplePut(req: CoroutineServerRequest) =
        CoroutineServerResponse.noContent().build()

    suspend fun simpleDelete(req: CoroutineServerRequest) =
        CoroutineServerResponse.noContent().build()
}
