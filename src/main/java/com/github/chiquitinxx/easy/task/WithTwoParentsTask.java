package com.github.chiquitinxx.easy.task;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * JFL 2019-06-07
 */
class WithTwoParentsTask<T, P1, P2> extends BaseTask<T> {

    private final Task<P1> parent1Task;
    private final Task<P2> parent2Task;
    private final BiFunction<P1, P2, T> function;
    private Supplier<T> supplier;
    private volatile P1 result1;
    private volatile P2 result2;

    WithTwoParentsTask(Task<P1> parent1, Task<P2> parent2, BiFunction<P1, P2, T> function, Consumer<TaskException> exceptionConsumer) {
        this.parent1Task = parent1;
        this.parent2Task = parent2;
        this.function = function;
        parent1.andThen(result -> {
            this.result1 = result;
            this.run();
        });
        parent2.andThen(result -> {
            this.result2 = result;
            this.run();
        });
        this.exceptionConsumer = exceptionConsumer;
    }

    @Override
    public void run() {
        if (this.result1 != null && this.result2 != null) {
            this.supplier = () -> this.function.apply(this.result1, this.result2);
            super.run();
        } else {
            if (this.result1 == null) {
                this.parent1Task.run();
            }
            if (this.result2 == null) {
                this.parent2Task.run();
            }
        }
    }

    @Override
    Supplier<T> getSupplier() {
        return this.supplier;
    }

    @Override
    void onFinishRun() {
        this.result1 = null;
        this.result2 = null;
    }
}
