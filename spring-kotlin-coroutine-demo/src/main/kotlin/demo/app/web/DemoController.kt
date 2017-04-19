package demo.app.web

import demo.app.service.DemoService
import demo.app.util.logger
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.kotlin.experimental.coroutine.annotation.Coroutine
import org.springframework.kotlin.experimental.coroutine.context.COMMON_POOL
import org.springframework.kotlin.experimental.coroutine.event.CoroutineApplicationEventPublisher
import org.springframework.kotlin.experimental.coroutine.web.client.CoroutineRestOperations
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
open class DemoController(
        private val demoService: DemoService,
        private val publisher: ApplicationEventPublisher,
        private val coroutinePublisher: CoroutineApplicationEventPublisher
) {
    private val restOperations = CoroutineRestOperations()

    @GetMapping("/delayed")
    suspend open fun delayedReturn(): String {
        logger.info ("Before call to [demoService.delayed]")
        val result = demoService.delayedReturn("delayed", 1000)
        logger.info ("After call to [demoService.delayed]")

        return result
    }

    @GetMapping("/commmonPool")
    suspend open fun commmonPoolReturn(): String {
        logger.info ("Before call to [demoService.commmonPoolReturn]")
        val result = demoService.commmonPoolReturn("commonPool")
        logger.info ("After call to [demoService.commmonPoolReturn]")

        return result
    }

    @GetMapping("/cachedDelayed")
    suspend open fun cachedDelayedReturn(): String {
        logger.info ("Before call to [demoService.cachedDelayedReturn]")
        val result = demoService.cachedDelayedReturn("cachedDelayed", 500)
        logger.info ("After call to [demoService.cachedDelayedReturn]")

        return result
    }

    @GetMapping("/cachedCommonPoolDelayed")
    suspend open fun cachedCommonPoolDelayed(): String {
        logger.info ("Before call to [demoService.cachedCommonPoolDelayedReturn]")
        val result = demoService.cachedCommonPoolDelayedReturn("cachedCommonPoolDelayed", 1500)
        logger.info ("After call to [demoService.cachedCommonPoolDelayedReturn]")

        return result
    }

    @GetMapping("/commonPoolController")
    @Coroutine(COMMON_POOL)
    suspend open fun commonPoolController(): String {
        logger.info ("In [commonPoolController]")

        return "commonPoolController"
    }

    @GetMapping("/event")
    suspend open fun event(): String {
        publisher.publishEvent(SimpleEvent("Hello"))
        publisher.publishEvent(DemoApplicationEvent(this, "Hello"))
        coroutinePublisher.publishEvent(SimpleEvent("Hello-coroutine"))
        coroutinePublisher.publishEvent(DemoApplicationEvent(this, "Hello-coroutine"))

        return "event"
    }

    @GetMapping("/rest")
    suspend open fun rest(request: HttpServletRequest): String {
        logger.info ("Before call to [restOperations.getForEntity]")
        val result = restOperations.getForEntity(request.requestURL.toString().replace("rest", "delayed"), String::class.java)
        logger.info ("After call to [restOperations.getForEntity]")

        return "Rest result: ${result.body}"
    }

    override fun toString(): String = "DemoController"

    companion object {
        private val logger = logger()
    }
}

open class DemoApplicationEvent(
    source: Any,
    val message: String
): ApplicationEvent(source) {

    override fun toString(): String = "DemoApplicationEvent(source=$source, message=$message)"
}

data class SimpleEvent(
    val message: String
)