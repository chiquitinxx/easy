package org.grooscript.easy;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * JFL 2019-04-25
 */
public class EasyTask<T> extends BaseTask<T> {

    private final Supplier<T> supplier;

    public static <R> Task<R> from(Supplier<R> supplier) {
        return new EasyTask<>(supplier);
    }

    public static <R> Task<R> from(Supplier<R> supplier, Consumer<TaskException> exceptionConsumer) {
        return new EasyTask<>(supplier, exceptionConsumer);
    }

    private EasyTask(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    private EasyTask(Supplier<T> supplier, Consumer<TaskException> exceptionConsumer) {
        this.supplier = supplier;
        this.exceptionConsumer = exceptionConsumer;
    }

    @Override
    Supplier<T> getSupplier() {
        return this.supplier;
    }
}
