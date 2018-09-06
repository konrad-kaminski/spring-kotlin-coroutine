package org.springframework.kotlin.experimental.coroutine


import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.kotlin.experimental.coroutine.web.WebConfiguration
import org.springframework.web.coroutine.function.client.CoroutineClientResponse
import org.springframework.web.coroutine.function.client.CoroutineWebClient
import org.springframework.web.coroutine.function.client.CoroutineWebClientUtils
import org.springframework.web.reactive.function.client.WebClientResponseException
import spock.lang.Specification

import static org.springframework.kotlin.experimental.coroutine.TestUtilsKt.runBlocking

@SpringBootTest(classes = [IntSpecConfiguration, WebConfiguration], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
class CoroutineWebClientSpec extends Specification {

    @LocalServerPort
    private int port

    def "should be able to access endpoint with CoroutineWebClient with custom header and body using retrieve()"() {
        when:
        final String url = "http://localhost:$port"
        final CoroutineWebClient.CoroutineResponseSpec spec = CoroutineWebClientUtils.createCoroutineWebClient(url)
                .post()
                .uri("/postWithHeaderAndBodyTest")
                .header("X-Coroutine-Test", "123456")
                .contentType(MediaType.APPLICATION_JSON)
                .body([text: "free-text", num: 1, flag: true], Map)
                .retrieve()

        final Map<String, Object> response = runBlocking { cont ->
            spec.body(Map, cont)
        }

        then:
        response["header"] == "123456"
        response["body"] == [text: "free-text", num: 1, flag: true]
    }

    def "should be able to access endpoint with CoroutineWebClient with custom header and body using exchange()"() {
        when:
        final String url = "http://localhost:$port"
        final CoroutineClientResponse resp = runBlocking { cont ->
            CoroutineWebClientUtils.createCoroutineWebClient(url)
                    .post()
                    .uri("/postWithHeaderAndBodyTest")
                    .header("X-Coroutine-Test", "123456")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body([text: "free-text", num: 1, flag: true], Map)
                    .exchange(cont)
        }

        final Map<String, Object> responseBody = runBlocking { cont ->
            resp.body(Map, cont)
        }

        then:
        resp.statusCode() == HttpStatus.CREATED
        responseBody["header"] == "123456"
        responseBody["body"] == [text: "free-text", num: 1, flag: true]
    }

    def "should fail to access endpoint with CoroutineWebClient with invalid content-type using exchange()"() {
        when:
        final String url = "http://localhost:$port"
        final CoroutineClientResponse resp = runBlocking { cont ->
            CoroutineWebClientUtils.createCoroutineWebClient(url)
                    .post()
                    .uri("/postWithHeaderAndBodyTest")
                    .contentType(MediaType.IMAGE_JPEG)
                    .exchange(cont)
        }

        then:
        resp.statusCode() == HttpStatus.UNSUPPORTED_MEDIA_TYPE
    }

    def "should fail to access endpoint with CoroutineWebClient with invalid content-type using retrieve()"() {
        when:
        final String url = "http://localhost:$port"
        runBlocking { cont ->
            CoroutineWebClientUtils.createCoroutineWebClient(url)
                    .post()
                    .uri("/postWithHeaderAndBodyTest")
                    .contentType(MediaType.IMAGE_GIF)
                    .retrieve()
                    .body(Map, cont)
        }

        then:
        def e = thrown WebClientResponseException
        e.statusCode == HttpStatus.UNSUPPORTED_MEDIA_TYPE
        e.statusText == "Unsupported Media Type"
    }
}