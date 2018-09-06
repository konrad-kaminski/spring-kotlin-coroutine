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

import org.json.JSONObject
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.kotlin.experimental.coroutine.IntSpecConfiguration
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import static org.springframework.http.MediaType.APPLICATION_JSON

@SpringBootTest(classes = [IntSpecConfiguration, FunctionalStyleRoutesConfiguration], webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
class FunctionalStyleRoutesIntSpec extends Specification {
    @LocalServerPort
    private int port

    private RestOperations restTemplate = new RestTemplate()

    def "should handle functional style defined GET request"() {
        when:
        def result = restTemplate.getForEntity("http://localhost:$port/test-functional/simple/HelloWorld", String)

        then:
        result.statusCode == HttpStatus.OK
        result.body == "HelloWorld"
    }

    def "should handle functional style defined POST request"() {
        when:
        def body = new JSONObject().put("key", "ping").toString()
        def request = RequestEntity.post(URI.create("http://localhost:$port/test-functional/simple")).contentType(APPLICATION_JSON).body(body)
        def result = restTemplate.exchange(request, String)

        then:
        result.statusCode == HttpStatus.CREATED
        result.headers.getLocation().toString() == "http://localhost:$port/test-functional/simple"
        result.body == new JSONObject().put("ping", "pong").toString()
    }

    def "should handle functional style defined PUT request"() {
        when:
        def body = new JSONObject().put("key", "ping").toString()
        def request = RequestEntity.put(URI.create("http://localhost:$port/test-functional/simple/1234"))
                .contentType(APPLICATION_JSON)
                .build()
        def result = restTemplate.exchange(request, String)

        then:
        result.statusCode == HttpStatus.NO_CONTENT
        result.body == null
    }

    def "should handle functional style defined DELETE request"() {
        when:
        def request = RequestEntity.delete(URI.create("http://localhost:$port/test-functional/simple/4321")).build()
        def result = restTemplate.exchange(request, String)

        then:
        result.statusCode == HttpStatus.NO_CONTENT
        result.body == null
    }
}
