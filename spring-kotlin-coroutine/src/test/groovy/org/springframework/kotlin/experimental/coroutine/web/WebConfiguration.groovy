package org.springframework.kotlin.experimental.coroutine.web

import org.springframework.context.annotation.Bean

class WebConfiguration {
    @Bean
    CoroutineController coroutineController() {
        return new CoroutineController()
    }
}

