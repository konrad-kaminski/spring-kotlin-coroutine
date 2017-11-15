package org.springframework.web.coroutine.function.client

internal object CoroutineWebClientUtils {
    @JvmStatic
    fun createCoroutineWebClient(url: String): CoroutineWebClient = CoroutineWebClient.create(url)
}