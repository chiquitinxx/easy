package org.grooscript.easy

import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

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
        EasyPublisher<Integer> publisher = easyPublisher({ Integer number -> atomic.set(number)})

        when:
        publisher.submit(5)

        then:
        conditions.eventually {
            assert atomic.get() == 5
        }
    }

    void 'submit call async to subscribers'() {
        given:
        PollingConditions conditions = new PollingConditions()
        EasyPublisher<Integer> publisher = easyPublisher({ Integer number ->
            sleep(25)
            atomic.set(number)
        })

        when:
        publisher.submit(5)

        then:
        atomic.get() == 0

        and:
        conditions.eventually {
            assert atomic.get() == 5
        }
    }

    private EasyPublisher<Integer> easyPublisher(Consumer<Integer> consumer) {
        EasyPublisher<Integer> publisher = new EasyPublisher()
        EasySubscriber<Integer> subscriber = new EasySubscriber(consumer)
        publisher.subscribe(subscriber)
        publisher
    }

    void setup() {
        atomic.set(initialAtomic)
    }

    private AtomicInteger atomic = new AtomicInteger()
    private static final Integer initialAtomic = 0
}
