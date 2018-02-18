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

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.kotlin.experimental.coroutine.IntSpecConfiguration
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM

@SpringBootTest(classes = [IntSpecConfiguration, WebConfiguration], webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
class WebIntSpec extends Specification {
    @LocalServerPort
    private int port

    private RestOperations restTemplate = new RestTemplate()

    private static def TEST_RESULT = """data:0

data:1

data:2

data:3

data:4"""

    @Unroll
    def "should handle REST controller GET coroutine with #resultType result"() {
        when:
        def headers = new HttpHeaders()
        headers.setAccept([acceptMediaType])
        def result = restTemplate.exchange("http://localhost:$port/$path", GET,
                new HttpEntity<Object>(headers), resultTypeClass)

        then:
        result.statusCode == HttpStatus.OK
        def resultValue = result.body
        if (resultTypeClass == String.class) resultValue = resultValue.trim()
        resultValue == expectedResultValue

        where:
        resultType          | path                              | acceptMediaType   | resultTypeClass | expectedResultValue
        "direct"            | "/multiply/3/4"                   | APPLICATION_JSON  | Integer.class   | 3*4
        "callback"          | "/suspendedMultiply/2/5"          | APPLICATION_JSON  | Integer.class   | 2*5
        "delayed"           | "/delayedMultiply/2/5"            | APPLICATION_JSON  | Integer.class   | 2*5
        "channel"           | "/channelMultiply/6/7"            | APPLICATION_JSON  | int[].class     | [6*7]
        "delayedChannel"    | "/delayedChannelMultiply/6/4"     | APPLICATION_JSON  | int[].class     | [6*4]
        "suspendChannel"    | "/suspendChannelMultiply/6/8"     | APPLICATION_JSON  | int[].class     | [6*8]
        "channelList"       | "/channelMultiplyList/2/9"        | APPLICATION_JSON  | int[].class     | [2*9]
        "sseChannel"        | "/sseChannelMultiply/6/7"         | TEXT_EVENT_STREAM | String.class    | "data:42"
        "sseDelayedChannel" | "/sseDelayedChannelMultiply/6/4"  | TEXT_EVENT_STREAM | String.class    | "data:24"
        "sseSuspendChannel" | "/sseSuspendChannelMultiply/6/8"  | TEXT_EVENT_STREAM | String.class    | "data:48"
        "sseChannelList"    | "/sseChannelMultiplyList/2/9"     | TEXT_EVENT_STREAM | String.class    | "data:[18]"
        "test"              | "/test"                           | TEXT_EVENT_STREAM | String.class    | TEST_RESULT
        "delayedTest"       | "/delayedTest"                    | TEXT_EVENT_STREAM | String.class    | TEST_RESULT
    }

    def "should handle REST controller POST coroutine"() {
        when:
        def result = restTemplate.postForEntity("http://localhost:$port/postTest", "", String)

        then:
        result.statusCode == HttpStatus.OK
        result.body == "123456"
    }

    def "should handle suspending controller functions which return view name"() {
        when:
        def result = restTemplate.getForEntity("http://localhost:$port/blog", String)

        then:
        result.statusCode == HttpStatus.OK
        result.body.trim() == "<article><title>TestTitle</title><text>TestText</text></article>"
    }

    @Ignore("Waiting for the fix in Spring 5")
    def "should handle suspending controller functions which return view name different from endpoint path"() {
        when:
        def result = restTemplate.getForEntity("http://localhost:$port/blogEndpoint", String)

        then:
        result.statusCode == HttpStatus.OK
        result.body.trim() == "<article><title>TestTitle</title><text>TestText</text></article>"
    }
}
