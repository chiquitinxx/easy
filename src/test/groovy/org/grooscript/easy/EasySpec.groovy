package org.grooscript.easy

import spock.lang.Specification
import spock.lang.Unroll
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

    @Unroll
    void 'multiple subscribers'() {
        given:
        PollingConditions conditions = new PollingConditions()
        EasyPublisher<Integer> publisher = new EasyPublisher()
        FIVE.times { addSubscriber(publisher, { number -> atomic.getAndAdd(FOUR * number) }) }

        when:
        publisher.submit(number)

        then:
        conditions.eventually {
            assert atomic.get() == FIVE * FOUR * number
        }

        where:
        number << [0, FOUR, FIVE, 1]
    }

    void 'multiple subscribers runs in different threads'() {
        given:
        PollingConditions conditions = new PollingConditions()
        EasyPublisher<Integer> publisher = new EasyPublisher()
        FIVE.times { addSubscriber(publisher, { number ->
            sleep(400)
            atomic.getAndAdd(FOUR * number) }
        )}

        when:
        publisher.submit(THREE)

        then:
        conditions.timeout == 1.0
        conditions.eventually {
            assert atomic.get() == FIVE * FOUR * THREE
        }
    }

    private EasyPublisher<Integer> easyPublisher(Consumer<Integer> consumer) {
        EasyPublisher<Integer> publisher = new EasyPublisher()
        addSubscriber(publisher, consumer)
    }

    private EasyPublisher<Integer> addSubscriber(EasyPublisher<Integer> publisher, Consumer consumer) {
        EasySubscriber<Integer> subscriber = new EasySubscriber(consumer)
        publisher.subscribe(subscriber)
        publisher
    }

    void setup() {
        atomic.set(initialAtomic)
    }

    private static final Integer THREE = 3
    private static final Integer FOUR = 4
    private static final Integer FIVE = 5
    private AtomicInteger atomic = new AtomicInteger()
    private static final Integer initialAtomic = 0
}
