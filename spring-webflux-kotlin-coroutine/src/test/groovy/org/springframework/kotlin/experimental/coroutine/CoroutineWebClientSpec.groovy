package org.springframework.kotlin.experimental.coroutine

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.kotlin.experimental.coroutine.web.WebConfiguration
import org.springframework.web.coroutine.function.client.CoroutineWebClient
import org.springframework.web.coroutine.function.client.CoroutineWebClientUtils
import spock.lang.Specification

import static org.springframework.kotlin.experimental.coroutine.TestUtilsKt.runBlocking

@SpringBootTest(classes = [IntSpecConfiguration, WebConfiguration], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
class CoroutineWebClientSpec extends Specification {
    @LocalServerPort
    private int port

    def "should be able to access endpoint with CoroutineWebClient"() {
        when:
        final String url = "http://localhost:$port"
        final CoroutineWebClient.CoroutineResponseSpec spec = runBlocking { cont ->
            CoroutineWebClientUtils.createCoroutineWebClient(url)
                    .post()
                    .uri("/postTest")
                    .retrieve(cont)
        }

        final String response = runBlocking { cont ->
            spec.body(String, cont)
        }

        then:
        response == "123456"
    }
}