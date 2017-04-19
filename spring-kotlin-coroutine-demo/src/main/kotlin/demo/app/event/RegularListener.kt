package demo.app.event

import demo.app.util.logger
import demo.app.web.DemoApplicationEvent
import demo.app.web.SimpleEvent
import org.springframework.context.ApplicationEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
open class RegularListener {
    @EventListener
    open fun handle(event: SimpleEvent) {
        logger.info ("Received event $event")
    }


    @EventListener
    open fun handleDemoEvent(event: DemoApplicationEvent) {
        logger.info ("Received demoEvent $event")
    }

    companion object {
        private val logger = logger()
    }
}