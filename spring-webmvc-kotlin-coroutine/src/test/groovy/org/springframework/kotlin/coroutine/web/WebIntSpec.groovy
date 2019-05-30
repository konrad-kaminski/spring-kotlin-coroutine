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

package org.springframework.kotlin.coroutine.web

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.kotlin.coroutine.IntSpecConfiguration
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest(classes = [IntSpecConfiguration, WebConfiguration], webEnvironment = org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
class WebIntSpec extends Specification {
    @LocalServerPort
    private int port

    private RestOperations restTemplate = new RestTemplate()

    @Unroll
    def "should handle REST controller coroutine with #resultType result"() {
        when:
        def result = restTemplate.getForEntity("http://localhost:$port/$path", Integer.TYPE)

        then:
        result.statusCode == HttpStatus.OK
        result.body == resultValue

        where:
        resultType | path                     | resultValue
        "direct"   | "/multiply/3/4"          | 3*4
        "callback" | "/suspendedMultiply/2/5" | 2*5
        "delayed"  | "/delayedMultiply/2/5"   | 2*5
    }
}
