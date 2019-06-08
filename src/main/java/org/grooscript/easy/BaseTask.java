package org.grooscript.easy;

import org.grooscript.easy.util.LinkedConsumers;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * JFL 2019-04-25
 */
public abstract class BaseTask<T> implements Task<T> {

    private static Consumer<TaskException> defaultExceptionConsumer =
            (taskException) -> {
                throw new RuntimeException(taskException);
            };

    Consumer<TaskException> exceptionConsumer;
    private LinkedConsumers<T> resultConsumers = new LinkedConsumers<>();

    abstract Supplier<T> getSupplier();
    abstract void onFinishRun();

    private Consumer<TaskException> getExceptionConsumer() {
        if (exceptionConsumer != null) {
            return exceptionConsumer;
        } else {
            return defaultExceptionConsumer;
        }
    }
    
    @Override
    public void run() {
        CompletableFuture.supplyAsync(() -> this.getSupplier().get())
                .thenApplyAsync( result -> {
                    this.resultConsumers.process(result);
                    return result;
                }).exceptionally(t -> {
                    t.printStackTrace();
                    getExceptionConsumer().accept(new TaskException(t));
                    return null;
                }).whenComplete((r, t) -> onFinishRun());
    }

    @Override
    public synchronized void andThen(Consumer<T> consumer) {
        this.resultConsumers.add(consumer);
    }

    @Override
    public synchronized <R> Task<R> then(Function<T, R> function) {
        return new WithParentTask<>(this, function, this.getExceptionConsumer());
    }

    @Override
    public synchronized <R, U> Task<R> combine(Task<U> task, BiFunction<T, U, R> biFunction) {
        return new WithTwoParentsTask<>(this, task, biFunction, this.getExceptionConsumer());
    }
}
