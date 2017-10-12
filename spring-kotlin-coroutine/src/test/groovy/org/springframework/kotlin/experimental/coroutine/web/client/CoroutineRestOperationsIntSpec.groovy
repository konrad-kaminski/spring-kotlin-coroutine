package org.springframework.kotlin.experimental.coroutine.web.client

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.kotlin.experimental.coroutine.IntSpecConfiguration
import org.springframework.kotlin.experimental.coroutine.web.TestWebConfiguration
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.kotlin.experimental.coroutine.TestUtilsKt.runBlocking
import static org.springframework.kotlin.experimental.coroutine.web.CoroutineRestOperationsIntSpecHelper.coroutineRestOperations

@SpringBootTest(classes = [IntSpecConfiguration, TestWebConfiguration], webEnvironment = RANDOM_PORT)
@EnableAutoConfiguration
class CoroutineRestOperationsIntSpec extends Specification {
    @LocalServerPort
    private int port

    @Unroll
    def "should handle REST controller with CoroutineRestOperations"() {
        when:
        def result = runBlocking { cont ->
            coroutineRestOperations.getForEntity("http://localhost:$port/multiply/5/7", Integer.TYPE, new Object[0], cont)
        }

        then:
        result.statusCode == HttpStatus.OK
        result.body == 5*7
    }
}