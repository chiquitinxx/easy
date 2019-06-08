package org.grooscript.easy;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * JFL 2019-04-23
 */
public interface Task<T> {
    void run();
    void andThen(Consumer<T> consumer);
    <R> Task<R> then(Function<T, R> function);
    //<R> Task<R> then(Function<T, R> function, Consumer<TaskException> exceptionConsumer);
    //<R> Task<R> then(FunctionWithExceptionConsumer<T, R> functionWithExceptionConsumer);
    <R, U> Task<R> join(Task<U> task, BiFunction<T, U, R> biFunction);
    //List<Task<?>> split(Function<T, ?>... functions);
}
