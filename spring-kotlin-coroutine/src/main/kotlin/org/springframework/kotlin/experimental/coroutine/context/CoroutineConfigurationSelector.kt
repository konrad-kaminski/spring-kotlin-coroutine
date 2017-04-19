package org.springframework.kotlin.experimental.coroutine.context

import org.springframework.context.annotation.AdviceMode
import org.springframework.context.annotation.AdviceMode.PROXY
import org.springframework.context.annotation.AdviceModeImportSelector
import org.springframework.kotlin.experimental.coroutine.EnableCoroutine

internal open class CoroutineConfigurationSelector : AdviceModeImportSelector<EnableCoroutine>() {
    override fun selectImports(adviceMode: AdviceMode): Array<String> =
            when (adviceMode) {
                PROXY -> arrayOf(ProxyCoroutineConfiguration::class.java.name)
                else  -> throw NotImplementedError()
            }
}
