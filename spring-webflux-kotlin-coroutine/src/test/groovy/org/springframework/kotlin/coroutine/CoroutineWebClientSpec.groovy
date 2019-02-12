package org.springframework.kotlin.coroutine

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.kotlin.coroutine.web.WebConfiguration
import org.springframework.web.coroutine.function.client.CoroutineWebClient
import org.springframework.web.coroutine.function.client.CoroutineWebClientUtils
import spock.lang.Specification

import static org.springframework.kotlin.coroutine.TestUtilsKt.runBlocking

@SpringBootTest(classes = [IntSpecConfiguration, org.springframework.kotlin.coroutine.web.WebConfiguration], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
class CoroutineWebClientSpec extends Specification {
    @LocalServerPort
    private int port

    def "should be able to access endpoint with CoroutineWebClient with custom header"() {
        when:
        final String url = "http://localhost:$port"
        final CoroutineWebClient.CoroutineResponseSpec spec = runBlocking { cont ->
            CoroutineWebClientUtils.createCoroutineWebClient(url)
                    .post()
                    .uri("/postWithHeaderTest")
                    .header("X-Coroutine-Test", "123456")
                    .retrieve(cont)
        }

        final String response = runBlocking { cont ->
            spec.body(String, cont)
        }

        then:
        response == "123456"
    }
}