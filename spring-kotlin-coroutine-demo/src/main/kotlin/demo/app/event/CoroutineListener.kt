package demo.app.event

import demo.app.util.logger
import demo.app.web.DemoApplicationEvent
import demo.app.web.SimpleEvent
import kotlinx.coroutines.experimental.delay
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
open class CoroutineListener {
    @EventListener
    suspend open fun handle(event: SimpleEvent) {
        delay(10)
        logger.info ("Received event $event")
    }

    @EventListener
    suspend open fun handleDemoEvent(event: DemoApplicationEvent) {
        delay(10)
        logger.info ("Received demoEvent $event")
    }

    companion object {
        private val logger = logger()
    }
}