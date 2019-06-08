package org.grooscript.easy;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * JFL 2019-06-07
 */
class WithParentTask<T, P> extends BaseTask<T> {

    private final Task<P> parentTask;
    private final Function<P, T> function;
    private Supplier<T> supplier;

    WithParentTask(Task<P> parent, Function<P, T> function, Consumer<TaskException> exceptionConsumer) {
        parent.andThen(this::processResult);
        this.parentTask = parent;
        this.function = function;
        this.exceptionConsumer = exceptionConsumer;
    }

    private void processResult(P parentResult) {
        this.supplier = (() -> this.function.apply(parentResult));
        this.run();
    }

    @Override
    public void run() {
        if (this.supplier == null) {
            this.parentTask.run();
        } else {
            super.run();
        }
    }

    @Override
    Supplier<T> getSupplier() {
        return this.supplier;
    }

    @Override
    void onFinishRun() {
        this.supplier = null;
    }
}
