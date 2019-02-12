package org.springframework.kotlin.coroutine.web.client

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.kotlin.coroutine.IntSpecConfiguration
import org.springframework.kotlin.coroutine.web.TestWebConfiguration
import org.springframework.web.client.HttpClientErrorException
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.kotlin.coroutine.TestUtilsKt.runBlocking
import static org.springframework.kotlin.coroutine.web.DeferredRestOperationsIntSpecHelper.deferredRestOperations

@SpringBootTest(classes = [IntSpecConfiguration, TestWebConfiguration], webEnvironment = RANDOM_PORT)
@EnableAutoConfiguration
class DeferredRestOperationsIntSpec extends Specification {
    @LocalServerPort
    private int port

    @Unroll
    def "should handle REST controller with DeferredRestOperations"() {
        when:
        def deferred = deferredRestOperations.getForEntity("http://localhost:$port/multiply/5/7", Integer.TYPE)
        def result = runBlocking { cont ->
            deferred.await(cont)
        }

        then:
        result.statusCode == HttpStatus.OK
        result.body == 5 * 7
    }

    def "should handle 404 error from REST controller with DeferredRestOperations"() {
        when:
        runBlocking { cont ->
            deferredRestOperations.getForEntity("http://localhost:$port/notfound", Integer.TYPE).await(cont)
        }

        then:
        def exception = thrown(HttpClientErrorException.class)
        exception.statusCode == HttpStatus.NOT_FOUND
    }
}