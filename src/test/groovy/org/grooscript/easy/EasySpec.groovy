package org.grooscript.easy

import org.spockframework.runtime.SpockTimeoutError
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier

/**
 * JFL 2019-04-08
 */
class EasySpec extends Specification {

    void 'atomic reset'() {
        expect:
        atomic.get() == INITIAL
    }

    void 'run a task'() {
        given:
        PollingConditions conditions = new PollingConditions()
        Task<Integer> task = EasyTask.from SET_ATOMIC_FOUR_AND_RETURN_FIVE

        when:
        task.run()

        then:
        conditions.eventually {
            assert atomic.get() == FOUR
        }
    }

    void 'task is lazy if not run'() {
        given:
        PollingConditions conditions = new PollingConditions()

        when:
        EasyTask.from SET_ATOMIC_FOUR_AND_RETURN_FIVE
        conditions.eventually {
            assert atomic.get() == FOUR
        }

        then:
        thrown(SpockTimeoutError)
    }

    void 'do something with the result of task'() {
        given:
        PollingConditions conditions = new PollingConditions()

        when:
        EasyTask.from(SET_ATOMIC_FOUR_AND_RETURN_FIVE).then { value ->
            int result = value * THREE
            atomic.set(result)
            result
        }.run()

        then:
        conditions.eventually {
            assert atomic.get() == FIVE * THREE
        }
    }

    void 'throws an exception in a task'() {
        given:
        PollingConditions conditions = new PollingConditions()
        Task task = EasyTask.from THROWS_EXCEPTION, { TaskException taskException -> atomic.set(EXCEPTION) }

        when:
        task.run()

        then:
        conditions.eventually {
            assert atomic.get() == EXCEPTION
        }
    }

    void 'do something after run a task'() {
        given:
        PollingConditions conditions = new PollingConditions()
        Task<Integer> task = EasyTask.from(SET_ATOMIC_FOUR_AND_RETURN_FIVE)

        when:
        task.andThen { value ->
            int result = value * FIVE
            atomic.set(result)
        }
        task.run()

        then:
        conditions.eventually {
            assert atomic.get() == FIVE * FIVE
        }
    }

    void 'run a task twice'() {
        given:
        PollingConditions conditions = new PollingConditions()
        Task<Integer> task = EasyTask.from(RETURN_FIVE)

        expect:
        atomic.get() == INITIAL

        when:
        int random = new Random().nextInt(TEN)
        task.andThen { value -> atomic.getAndAdd(value * random) }
        int expectedFirstResult = FIVE * random
        task.run()

        then:
        conditions.eventually { assert atomic.get() == expectedFirstResult }

        when:
        random = new Random().nextInt(TEN) + TEN
        task.run()

        then:
        conditions.eventually { assert atomic.get() == expectedFirstResult + (FIVE * random) }
    }

    void 'run a task with parent twice'() {
        given:
        PollingConditions conditions = new PollingConditions()
        Task<Integer> task = EasyTask.from(RETURN_FIVE)
        int random = new Random().nextInt(TEN)
        Task<Integer> secondTask = task.then { value -> atomic.getAndAdd(value * random) }

        when:
        int expectedFirstResult = FIVE * random
        secondTask.run()

        then:
        conditions.eventually { assert atomic.get() == expectedFirstResult }

        when:
        random = new Random().nextInt(TEN) + TEN
        secondTask.run()

        then:
        conditions.eventually { assert atomic.get() == expectedFirstResult + (FIVE * random) }
    }

    @Unroll
    void 'a run in task #taskToRun run all the chain'() {
        given:
        PollingConditions conditions = new PollingConditions()
        Task<Integer> firstTask = EasyTask.from(SET_ATOMIC_FOUR_AND_RETURN_FIVE)
        Task<Integer> secondTask = firstTask.then { number -> number * 2 }
        Task<Integer> thirdTask = secondTask.then { number -> number * 3 }
        Task<Integer> finalTask = thirdTask.then { number -> atomic.set(number); number }
        Map<String, Task> tasks = [
                firstTask: firstTask, secondTask: secondTask, thirdTask: thirdTask, finalTask: finalTask
        ]

        when:
        tasks[taskToRun].run()

        then:
        conditions.eventually {
            assert atomic.get() == 30
        }

        where:
        taskToRun << ['firstTask', 'secondTask', 'thirdTask', 'finalTask']
    }

    @Unroll
    void 'combine two tasks, running task #taskToRun'() {
        given:
        PollingConditions conditions = new PollingConditions()
        Task<Integer> taskFive = EasyTask.from(RETURN_FIVE)
        Task<Integer> taskFour = EasyTask.from(RETURN_FOUR)
        Task<Integer> combined = taskFive.combine(taskFour, { first, second ->
            Integer value = first - second
            atomic.set(value)
            value
        })
        Map<String, Task> tasks = [
                taskFive: taskFive, taskFour: taskFour, combined: combined
        ]

        when:
        tasks[taskToRun].run()

        then:
        conditions.eventually { assert atomic.get() == 1 }

        where:
        taskToRun << ['taskFive', 'taskFour', 'combined']
    }

    void 'combine a task twice'() {
        given:
        PollingConditions conditions = new PollingConditions()
        int random = new Random().nextInt(TEN)
        Task<Integer> task1 = EasyTask.from { -> random * 2 }
        Task<Integer> task2 = EasyTask.from { -> random * 3 }
        Task<Integer> combine = task1.combine(task2, { a, b ->
            Integer result = b - a
            atomic.set(result)
            result
        })

        when:
        combine.run()

        then:
        conditions.eventually { assert atomic.get() == random }

        when:
        random = new Random().nextInt(TEN) + TEN
        combine.run()

        then:
        conditions.eventually { assert atomic.get() == random }
    }

    void setup() {
        atomic.set(INITIAL)
    }

    private static final Supplier<Integer> THROWS_EXCEPTION = { -> throw new RuntimeException('test') }
    private static final Integer EXCEPTION = -1
    private static final Integer THREE = 3
    private static final Integer FOUR = 4
    private static final Integer FIVE = 5
    private static final Integer TEN = 10
    private static final Integer INITIAL = 0
    private Supplier<Integer> RETURN_FIVE = { -> FIVE }
    private Supplier<Integer> RETURN_FOUR = { -> FOUR }
    private Supplier<Integer> SET_ATOMIC_FOUR_AND_RETURN_FIVE = { -> atomic.set(FOUR); FIVE }
    private AtomicInteger atomic = new AtomicInteger()
}
