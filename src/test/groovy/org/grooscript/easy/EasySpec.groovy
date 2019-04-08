package org.grooscript.easy

import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.atomic.AtomicInteger

/**
 * JFL 2019-04-08
 */
class EasySpec extends Specification {

    void 'nothing happens if no subscriber'() {
        given:
        EasyPublisher<Integer> publisher = new EasyPublisher<>()

        expect:
        !publisher.isClosed()
        !publisher.hasSubscribers()

        when:
        publisher.submit(5)

        then:
        publisher.isClosed()
    }

    void 'when the publisher submit, subscribers receives data'() {
        given:
        PollingConditions conditions = new PollingConditions()
        EasyPublisher<Integer> publisher = new EasyPublisher()
        EasySubscriber<Integer> subscriber = new EasySubscriber({ Integer number -> atomic.set(number)})
        publisher.subscribe(subscriber)

        when:
        publisher.submit(5)

        then:
        conditions.eventually {
            assert atomic.get() == 5
        }
    }

    void setup() {
        atomic.set(0)
    }

    private AtomicInteger atomic = new AtomicInteger()
}
