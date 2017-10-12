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

package org.springframework.kotlin.experimental.coroutine.context

import org.springframework.context.annotation.AdviceMode
import org.springframework.context.annotation.AdviceMode.PROXY
import org.springframework.context.annotation.AdviceModeImportSelector
import org.springframework.core.io.UrlResource
import org.springframework.kotlin.experimental.coroutine.EnableCoroutine
import java.net.URL
import java.util.Enumeration
import kotlin.streams.asSequence

internal open class CoroutineConfigurationSelector : AdviceModeImportSelector<EnableCoroutine>() {
    override fun selectImports(adviceMode: AdviceMode): Array<String> =
            (loadCoroutineConfigurations() + getAdviceModeConfiguration(adviceMode)).toList().toTypedArray()

    private fun loadCoroutineConfigurations(): Sequence<String> =
        CoroutineConfigurationSelector::class.java.classLoader.getResources("coroutine.configurations").asSequence()
                .flatMap(this::getLines)
                .filterNot(CharSequence::isEmpty)

    private fun getLines(url: URL): Sequence<String> =
        UrlResource(url).inputStream.bufferedReader().lines().asSequence()

    private fun getAdviceModeConfiguration(adviceMode: AdviceMode): Sequence<String> =
        when (adviceMode) {
            PROXY -> sequenceOf(ProxyCoroutineConfiguration::class.java.name)
            else  -> throw NotImplementedError()
        }
}

private fun <T: Any> Enumeration<T>.asSequence(): Sequence<T> = generateSequence {
    if (hasMoreElements()) nextElement() else null
}
