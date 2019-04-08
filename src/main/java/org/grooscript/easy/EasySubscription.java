package org.grooscript.easy;

import java.util.concurrent.Flow;
import java.util.function.Consumer;

/**
 * JFL 2019-04-07
 */
public class EasySubscription<T> implements Flow.Subscription {

    private boolean requested = false;
    private boolean cancelled = false;
    private Consumer<T> consumer;

    public EasySubscription() {
        this.request(1);
    }

    @Override
    public void request(long n) {
        requested = true;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    public EasySubscription setAction(Consumer<T> consumer) {
        this.consumer = consumer;
        return this;
    }

    public EasySubscription doWith(T item) {
        if (requested && !cancelled) {
            this.consumer.accept(item);
        }
        return this;
    }
}
