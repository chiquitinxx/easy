package org.grooscript.easy;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * JFL 2019-04-24
 */
public class FunctionWithExceptionConsumer<T, R> {
    private final Function<T, R> function;
    private final Consumer<TaskException> exceptionConsumer;

    public FunctionWithExceptionConsumer(Function<T, R> function, Consumer<TaskException> exceptionConsumer) {
        this.function = function;
        this.exceptionConsumer = exceptionConsumer;
    }

    public Function<T, R> getFunction() {
        return this.function;
    }

    public Consumer<TaskException> getExceptionConsumer() {
        return this.exceptionConsumer;
    }
}
