package org.springframework.kotlin.experimental.coroutine.event

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.kotlin.experimental.coroutine.IntSpecConfiguration
import spock.lang.Specification

import java.util.function.Function

import static org.springframework.kotlin.experimental.coroutine.TestUtilsKt.runBlocking

@SpringBootTest(classes = [IntSpecConfiguration, EventSpecConfiguration])
class CoroutineApplicationEventPublisherIntSpec extends Specification {
    @Autowired
    private CoroutineApplicationEventPublisher coroutinePublisher

    @Autowired
    private ApplicationEventPublisher eventPublisher

    @Autowired
    private CoroutineListener coroutineListener

    @Autowired
    private RegularListener regularListener

    private def coroutineListenerMock = Mock(Function)

    private regularListenerMock = Mock(Function)

    def setup() {
        coroutineListener.reset(coroutineListenerMock)
        regularListener.reset(regularListenerMock)
    }

    def "should receive simple event sent by coroutine publisher"() {
        when:
        runBlocking { cont ->
            coroutinePublisher.publishEvent("Hello", cont)
        }

        then:
        coroutineListener.events == ["Hello"]
        regularListener.events == ["Hello"]
    }

    def "should receive simple event sent by event publisher"() {
        when:
        eventPublisher.publishEvent("Hello")

        then:
        coroutineListener.events == ["Hello"]
        regularListener.events == ["Hello"]
    }

    def "should receive application event by regular listener sent by event publisher"() {
        given:
        def testEvent = new TestEvent("Hello")

        when:
        eventPublisher.publishEvent(testEvent)

        then:
        coroutineListener.events == [testEvent]
        coroutineListener.testEvents == [testEvent]
        regularListener.events == [testEvent]
        regularListener.testEvents == [testEvent]
    }

    def "should receive application event by regular listener sent by coroutine publisher"() {
        given:
        def testEvent = new TestEvent("Hello")

        when:
        runBlocking { cont ->
            coroutinePublisher.publishEvent(testEvent, cont)
        }

        then:
        coroutineListener.events == [testEvent]
        coroutineListener.testEvents == [testEvent]
        regularListener.events == [testEvent]
        regularListener.testEvents == [testEvent]
    }

    def "should republish simple event coroutine listener result when original has been sent by coroutine publisher"() {
        given:
        coroutineListenerMock.apply("Hello") >> "Hi"

        when:
        runBlocking { cont ->
            coroutinePublisher.publishEvent("Hello", cont)
        }

        then:
        coroutineListener.events == ["Hello", "Hi"]
        regularListener.events as Set == ["Hello", "Hi"] as Set
    }

    def "should republish simple event coroutine listener result when original has been sent by event publisher"() {
        given:
        coroutineListenerMock.apply("Hello") >> "Hi"

        when:
        eventPublisher.publishEvent("Hello")

        then:
        coroutineListener.events == ["Hello", "Hi"]
        regularListener.events as Set == ["Hello", "Hi"] as Set
    }

    def "should republish simple event regular listener result when original has been sent by coroutine publisher"() {
        given:
        regularListenerMock.apply("Hello") >> "Hi"

        when:
        runBlocking { cont ->
            coroutinePublisher.publishEvent("Hello", cont)
        }

        then:
        regularListener.events == ["Hello", "Hi"]
        coroutineListener.events as Set == ["Hello", "Hi"] as Set
    }

    def "should republish simple event regular listener result when original has been sent by event publisher"() {
        given:
        regularListenerMock.apply("Hello") >> "Hi"

        when:
        eventPublisher.publishEvent("Hello")

        then:
        regularListener.events == ["Hello", "Hi"]
        coroutineListener.events as Set == ["Hello", "Hi"] as Set
    }

    def "should republish test event regular listener result when original has been sent by event publisher"() {
        given:
        def event = new TestEvent("Hello")
        regularListenerMock.apply(event) >> "Hi"

        when:
        eventPublisher.publishEvent(event)

        then:
        regularListener.events as Set == [event, "Hi"] as Set
        regularListener.events.size() == 3
        regularListener.testEvents == [event]
        coroutineListener.events as Set == [event, "Hi"] as Set
        coroutineListener.events.size() == 3
        coroutineListener.testEvents == [event]
    }

    def "should republish test event regular listener result when original has been sent by coroutine publisher"() {
        given:
        def event = new TestEvent("Hello")
        def outEvent = new TestEvent("Hi")
        regularListenerMock.apply(event) >> [outEvent, outEvent]

        when:
        runBlocking { cont ->
            coroutinePublisher.publishEvent(event, cont)
        }

        then:
        regularListener.events as Set == [event, outEvent] as Set
        regularListener.events.size() == 5
        regularListener.testEvents as Set == [event, outEvent] as Set
        regularListener.testEvents.size() == 5
        coroutineListener.events as Set == [event, outEvent] as Set
        coroutineListener.events.size() == 5
        coroutineListener.testEvents as Set == [event, outEvent] as Set
        coroutineListener.testEvents.size() == 5
    }
}

